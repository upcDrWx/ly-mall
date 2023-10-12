package com.leyou.page.service;

import com.leyou.item.pojo.*;
import com.leyou.page.client.BrandClient;
import com.leyou.page.client.CategoryClient;
import com.leyou.page.client.GoodsClient;
import com.leyou.page.client.SpecificationClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
public class PageService {
    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private SpecificationClient specClient;

    @Autowired
    private TemplateEngine templateEngine;

    public Map<String, Object> loadModel(Long spuId) {
        // 模型数据
        Map<String, Object> modelMap = new HashMap<>();

        // 查询spu
        Spu spu = goodsClient.querySpuById(spuId);
        if(spu==null){
            System.out.println("该商品不存在");
          return null;
        }
        //查询skus
        List<Sku> skus = spu.getSkus();
        //查询详情
        SpuDetail detail = spu.getSpuDetail();
        //查询brand
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        //查询商品分类
        List<Category> categories = categoryClient.queryCategoryByIds(
                Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        //查询规格参数
        List<SpecGroup> specs = specClient.queryGroupByCid(spu.getCid3());

        // 装填模型数据
        modelMap.put("title", spu.getTitle());
        modelMap.put("subTitle", spu.getSubTitle());
        modelMap.put("skus", skus);
        modelMap.put("detail", detail);
        modelMap.put("brand", brand);
        modelMap.put("categories", categories);
        modelMap.put("specs", specs);
        return modelMap;
    }
    public void creatHtml(Long spuId){
        //上下文
        Context context = new Context();
        context.setVariables(loadModel(spuId));
        //输出流
        File dest = new File("F:\\IdeaProjects\\HwjProjects\\upload2", spuId + ".html");

        if(dest.exists()){
            dest.delete();
        }
        try (PrintWriter writer=new PrintWriter(dest,"UTF-8")){
            //生成HTML
            templateEngine.process("item",context,writer);
        }catch (Exception e){
            log.error("[静态页服务] 生成静态页异常",e);
        }
    }
        public void deleteHtml(Long spuId) {
        File dest = new File("F:\\IdeaProjects\\HwjProjects\\upload2", spuId + ".html");
        if(dest.exists()){
            dest.delete();
        }
    }
}


