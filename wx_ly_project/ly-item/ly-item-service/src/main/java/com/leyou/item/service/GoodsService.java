package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.mapper.StockMapper;
import com.leyou.item.pojo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class GoodsService {
    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private SpuDetailMapper detailMapper;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    //注入mq模板
    @Autowired
    private AmqpTemplate amqpTemplate;

    public PageResult<Spu> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key) {
        //分页
        PageHelper.startPage(page,rows);
        //过滤
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        //搜索字段过滤
        if(StringUtils.isNotBlank(key)){
            criteria.andLike("title","%"+key+"%");
        }
        //上下架过滤
        if(saleable!=null){
            criteria.andEqualTo("saleable",saleable);
        }
        //默认排序
        example.setOrderByClause("last_update_time DESC");

        //查询
        List<Spu> spus=spuMapper.selectByExample(example);


        //判断
        if(CollectionUtils.isEmpty(spus)){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }

        //解析分类和品牌名称
        loadCaegroaryAndBrandName(spus);
        
        //解析分页结果
        PageInfo<Spu> info = new PageInfo<>(spus);
        return new PageResult<>(info.getTotal(),spus);
    }

    private void loadCaegroaryAndBrandName(List<Spu> spus) {
        for (Spu spu:spus){
            //处理分类名称
            List<String> names = categoryService.queryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()))
                    .stream().map(Category::getName).collect(Collectors.toList());
            spu.setCname(StringUtils.join(names,"/"));
            //处理品牌名称
            System.out.println("该品牌id:"+spu.getBrandId());
            //这里判断品牌id是否为0，因为查到这种情况会抛出“品牌不存在”的异常，导致页面刷不出来，这里是自己添加的
//            if(spu.getBrandId()!=0){
                spu.setBname(brandService.queryById(spu.getBrandId()).getName());
//            }
        }
    }

    /**
     * 新增商品
     * @param spu
     */
    @Transactional
    public void saveGoods(Spu spu) {
        //新增spu
        spu.setId(null);//自增
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        spu.setSaleable(true);
        spu.setValid(false);

        int count = spuMapper.insert(spu);
        if(count!=1){
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROE);
        }
        //新增detail
        SpuDetail detail = spu.getSpuDetail();
        detail.setSpuId(spu.getId());
        detailMapper.insert(detail);

       //新增sku和库存
        saveSkuAndStock(spu);

        //发送mq消息
        try{
            amqpTemplate.convertAndSend("item.insert",spu.getId());
        }catch(Exception e){
            e.getMessage();
        }
    }

    /**
     * 新增sku和库存
     * @param spu
     */
    private void saveSkuAndStock(Spu spu){
        int count;
        //定义库存集合
        List<Stock> stockList= new ArrayList<>();
        //新增sku
        List<Sku> skus = spu.getSkus();
        for(Sku sku:skus){
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            sku.setSpuId(spu.getId());

            count = skuMapper.insert(sku);
            if(count!=1){
                throw new LyException(ExceptionEnum.GOODS_SAVE_ERROE);
            }

            //新增库存
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());

            stockList.add(stock);

        }
        //批量新增库存
        count=stockMapper.insertList(stockList);
        if(count!=1){
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROE);
        }
    }
    public SpuDetail queryDetailById(Long spuId) {
        SpuDetail detail = detailMapper.selectByPrimaryKey(spuId);
        if(detail==null){
            throw new LyException(ExceptionEnum.GOODS_DETAIL_NOT_FOUND);
        }
        return  detail;
    }

    public List<Sku> querySkuBySpuId(Long spuId) {
        //查询sku
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> skuList = skuMapper.select(sku);
        if(CollectionUtils.isEmpty(skuList)){
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }
        //查询库存

        List<Long> ids = skuList.stream().map(Sku::getId).collect(Collectors.toList());
        List<Stock> stockList = stockMapper.selectByIdList(ids);
        if(CollectionUtils.isEmpty(stockList)){
            throw new LyException(ExceptionEnum.GOODS_STOCK_NOT_FOUND);
        }
        //我们把stock变成一个map,其key是：sku的id,值是库存值
        Map<Long, Integer> stockMap = stockList.stream()
                .collect(Collectors.toMap(Stock::getSkuId, Stock::getStock));
        skuList.forEach(s->s.setStock(stockMap.get(s.getId())));
        return skuList;
    }

    /**
     * 修改商品信息
     * @param spu
     */
    @Transactional
    public void updateGoods(Spu spu) {
        if(spu.getId()==null){
            throw new LyException(ExceptionEnum.GOODS_ID_CANNOT_BE_NULL);
        }
        Sku sku = new Sku();
        sku.setSpuId(spu.getId());

        //查询sku
        List<Sku> skuList = skuMapper.select(sku);
        if(!CollectionUtils.isEmpty(skuList)){
            //删除sku
            skuMapper.delete(sku);
            //删除stock
            List<Long> ids = skuList.stream().map(Sku::getId).collect(Collectors.toList());
            stockMapper.deleteByIdList(ids);

        }

        //修改spu
        spu.setValid(null);
        spu.setSaleable(null);
        spu.setLastUpdateTime(new Date());
        spu.setCreateTime(null);

        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if(count!=1){
            System.out.println("//修改spu");
            throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROE);
        }

        //修改detail
        count= detailMapper.updateByPrimaryKeySelective(spu.getSpuDetail());
        if(count!=1){
            System.out.println("//修改detail");
            throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROE);
        }
        //新增sku和stock
        saveSkuAndStock(spu);

        //发送mq消息
        try{
            amqpTemplate.convertAndSend("item.update",spu.getId());
        }catch (Exception e){
            e.getMessage();

        }
    }


    public Spu querySpuById(Long id) {
        //查询spu
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null) {
            throw new  LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //查询sku
        spu.setSkus(querySkuBySpuId(id));
        //查询detail
        spu.setSpuDetail(queryDetailById(id));
        return spu;
    }

    /**
     * 商品删除二合一（多个单个）
     * @param id
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteGoods(long id) {
        //删除spu表中的数据
        this.spuMapper.deleteByPrimaryKey(id);

        //删除spu_detail中的数据
        Example example = new Example(SpuDetail.class);
        example.createCriteria().andEqualTo("spuId",id);
        detailMapper.deleteByExample(example);

        List<Sku> skuList = this.skuMapper.selectByExample(example);
        for (Sku sku : skuList){
            //删除sku中的数据
            this.skuMapper.deleteByPrimaryKey(sku.getId());
            //删除stock中的数据
            this.stockMapper.deleteByPrimaryKey(sku.getId());
        }

    }



    /**
     * 根据skuId查询sku
     * @param id
     * @return
     */
    public Sku querySkuById(Long id) {
        return skuMapper.selectByPrimaryKey(id);
    }


    /**
     * 根据sku的集合查询所有sku
     * @param ids
     * @return
     */
    public List<Sku> querySkuByIds(List<Long> ids) {
        List<Sku> skus = skuMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(skus)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //填充库存
        fillStock(ids, skus);
        return skus;
    }

    private void fillStock(List<Long> ids, List<Sku> skus) {
        //批量查询库存
        List<Stock> stocks = stockMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(stocks)) {
            throw new LyException(ExceptionEnum.STOCK_NOT_FOUND);
        }
        //首先将库存转换为map，key为sku的ID
        Map<Long, Integer> map = stocks.stream().collect(Collectors.toMap(s -> s.getSkuId(), s -> s.getStock()));

        //遍历skus，并填充库存
        for (Sku sku : skus) {
            sku.setStock(map.get(sku.getId()));
        }
    }

    @Transactional
    public void decreaseStock(List<CartDTO> carts) {
        /*
        * 这里不建议用锁，这样就把减库存当做单线程操作了，同一时刻只能一个人操作
        * 性能很差，高并发时不能满足要求
        * 是悲观锁
        * 有线程安全问题
        * */
        /*
        * 因此我们使用乐观锁，不做查询，不作判断，直接就减库存
        * 在SQL语句中加入判断 where sku_id = #{skuId} and stock >= #{num}，
        * 这样有一百个人抢，执行成功的条件就是stock满足条件
        * */
        for (CartDTO cartDto : carts) {
            int count = stockMapper.decreaseStock(cartDto.getSkuId(), cartDto.getNum());
            if (count != 1) {
                throw new LyException(ExceptionEnum.STOCK_NOT_ENOUGH);
            }
        }
    }

    /**
     * 商品下架
     * @param id
     */
    @Transactional(rollbackFor = Exception.class)
    public void goodsSoldOut(Long id) {
        //下架或者上架spu中的商品

        Spu oldSpu = this.spuMapper.selectByPrimaryKey(id);
        Example example = new Example(Sku.class);
        example.createCriteria().andEqualTo("spuId",id);
        List<Sku> skuList = this.skuMapper.selectByExample(example);
        if (oldSpu.getSaleable()){
            //下架
            oldSpu.setSaleable(false);
            this.spuMapper.updateByPrimaryKeySelective(oldSpu);
            //下架sku中的具体商品
            for (Sku sku : skuList){
                sku.setEnable(false);
                this.skuMapper.updateByPrimaryKeySelective(sku);
            }

        }else {
            //上架
            oldSpu.setSaleable(true);
            this.spuMapper.updateByPrimaryKeySelective(oldSpu);
            //上架sku中的具体商品
            for (Sku sku : skuList){
                sku.setEnable(true);
                this.skuMapper.updateByPrimaryKeySelective(sku);
            }
        }

    }

}
