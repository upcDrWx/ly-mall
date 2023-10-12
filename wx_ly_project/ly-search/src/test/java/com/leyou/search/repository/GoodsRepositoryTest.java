package com.leyou.search.repository;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Spu;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.testGoods;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
@RunWith(SpringRunner.class)
@SpringBootTest
public class GoodsRepositoryTest {
    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ElasticsearchTemplate template;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SearchService searchService;

    /**
     * 创建索引库
     */
    @Test
    public void testCreateIndex(){
        template.createIndex(Goods.class);
        template.putMapping(Goods.class);
//        //创建索引
//          template.createIndex(testGoods.class);
//          //配置映射
//          template.putMapping(testGoods.class);
    }

    /**
     * 插入索引库数据
     */
    @Test
    public void loadData1() {
        int page = 1;
        int size = 0;
        int rows = 100;
        do {
            // 查询spu信息
            PageResult<Spu> result = goodsClient.querySpuByPage(page, rows, true, null);
            List<Spu> spuList = result.getItems();
            // 构建成goods
//            List<Goods> goodList = spuList.stream()
//                    .map(searchService::buildGoods).collect(Collectors.toList());
//            this.goodsRepository.saveAll(goodList);
            List<Goods> goodList = spuList.stream()
                    .map(searchService::buildGoods).collect(Collectors.toList());
            this.goodsRepository.saveAll(goodList);
            page++;
            size = spuList.size();
        } while (size == 100);
    }
//    @Test
//    public void loadData(){
//        int page = 1;
//        int rows =100;
//        int size = 0;
//        do {
//            //查询spu信息
//            PageResult<Spu> result = goodsClient.querySpuByPage(page, rows, true, null);
//            //取出当前页结果
//            List<Spu> spuList = result.getItems();
//            if(CollectionUtils.isEmpty(spuList)){
//                break;
//            }
//            //构建成goods
//            List<Goods> goodsList = spuList.stream().map(searchService::buildGoods).collect(Collectors.toList());
//            //存入索引库
//            goodsRepository.saveAll(goodsList);
//            //翻页
//            page++;
//            size = spuList.size();
//        }while(size == 100);
//    }

}