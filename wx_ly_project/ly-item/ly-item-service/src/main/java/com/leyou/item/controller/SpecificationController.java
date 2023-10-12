package com.leyou.item.controller;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.pojo.Specification;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("spec")
public class SpecificationController {
    @Autowired
    private SpecificationService specService;

    /**
     * 根据分类id查询规格组
     * @param cid
     * @return
     */
    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> querySpecGroups(@PathVariable("cid") Long cid){
        return ResponseEntity.ok(specService.querySpecGroupByCid(cid));
    }

    /**
     * 查询参数集合
     * @param gid 组id
     * @param cid 分类id
     * @param searching 是否搜索
     * @return
     */
    @GetMapping("/params")
    public ResponseEntity<List<SpecParam>> querySpecParamList(
            @RequestParam(value="gid", required = false) Long gid,
            @RequestParam(value="cid", required = false) Long cid,
            @RequestParam(value="searching", required = false) Boolean searching
    ){
        return ResponseEntity.ok(specService.querySpecParamList(gid,cid,searching));
    }

    /**
     * 根据分类查询规格组及组内参数
     * @param cid
     * @return
     */
    @GetMapping("group")
    public ResponseEntity<List<SpecGroup>> queryListByCid(@RequestParam("cid") Long cid){
        return ResponseEntity.ok(specService.queryListByCid(cid));
    }

//    /**
//     * 查询商品分类对应的规格参数模板
//     * @param id
//     * @return
//     */
//    @GetMapping("{id}")
//    public ResponseEntity<String> querySpecificationByCategoryId(@PathVariable("id") Long id){
//        Specification spec = this.specificationService.queryById(id);
//        if (spec == null) {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//        return ResponseEntity.ok(spec.getSpecifications());
//    }

    /**
     * 保存一个规格参数模板
     * @param specification
     * @return
     */
    @PostMapping("group")
    public ResponseEntity<Void> saveSpecification(Specification specification){
        this.specService.saveSpecification(specification);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * 修改一个规格参数模板
     * @param specification
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateSpecification(Specification specification){
        this.specService.updateSpecification(specification);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteSpecification(@PathVariable("id") Long id){
        Specification specification = new Specification();
        specification.setCategoryId(id);
        this.specService.deleteSpecification(specification);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
