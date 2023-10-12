package com.leyou.item.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;


@Service
public class BrandService {
    @Autowired
    private BrandMapper brandMapper;

    public PageResult<Brand> queryBrandByPage(
            Integer page, Integer rows, String sortBy, Boolean desc, String search) {
        //分页
        PageHelper.startPage(page,rows);
        /**
         * WHERE 'name' LIKE "%x%" OR letter == 'x'
         * ORDER BY id DESC
         * */
        //过滤
        Example example = new Example(Brand.class);
        if(StringUtils.isNotBlank(search)){
            //过滤条件
            example.createCriteria().orLike("name", "%"+search+"%")
                    .orEqualTo("letter", search.toUpperCase());
        }
        //排序
        if (StringUtils.isNotBlank(sortBy)) {
            example.setOrderByClause(sortBy + (desc ? " DESC" : " ASC"));
        }
        //查询
        List<Brand> brands = this.brandMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(brands)) {
//            System.out.println("哈哈哈哈哈哈哈哈哈哈，发现了1");
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        //创建PageInfo,解析分页结果，获得总条数Total
        PageInfo<Brand> info=new PageInfo<>(brands);

        brandMapper.selectByExample(example);
        return new PageResult<>(info.getTotal(),brands);
    }

    /**
     * 新增品牌
     * @param brand
     * @param cids
     */
    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        // 新增品牌信息
        brand.setId(null);
        int count = this.brandMapper.insert(brand);
        if(count!=1){
           throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
        }
        // 新增品牌和分类中间表
        for (Long cid : cids) {
            this.brandMapper.insertCategoryBrand(cid, brand.getId());
            if(count!=1){
                throw new LyException(ExceptionEnum.CATEGROY_BRAND_SAVE_ERROR);
            }
        }
    }

    /**
     * 品牌更新
     * @param brand
     * @param categories
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateBrand(Brand brand,List<Long> categories) {
//        System.out.println("bid:hhhhhhhhhhhhhhhhhhhh哈哈哈哈"+brand);
;        //删除原来的数据
        deleteByBrandIdInCategoryBrand(brand.getId());
        // 修改品牌信息
        this.brandMapper.updateByPrimaryKeySelective(brand);

        //维护品牌和分类中间表
        for (Long cid : categories) {
            //System.out.println("cid:"+cid+",bid:"+brand.getId());
            this.brandMapper.insertCategoryBrand(cid, brand.getId());
        }
    }


    public Brand queryById(Long id){
        Brand brand = brandMapper.selectByPrimaryKey(id);
        if(brand == null){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brand;
    }

    public List<Brand> queryBrandByCid(Long cid) {
        List<Brand> list = brandMapper.queryByCategoryId(cid);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return list;
    }

    public List<Brand> queryByIds(List<Long> ids) {
        List<Brand> brands = brandMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(brands)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brands;
    }

    /**
     * 品牌删除
     * @param id
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteBrand(Long id) {
        //删除品牌信息
        brandMapper.deleteByPrimaryKey(id);

        //维护中间表
        brandMapper.deleteByBrandIdInCategoryBrand(id);
    }

    /**
     * 删除中间表中的数据
     * @param bid
     */
    public void deleteByBrandIdInCategoryBrand(Long bid) {
        brandMapper.deleteByBrandIdInCategoryBrand(bid);
    }

}
