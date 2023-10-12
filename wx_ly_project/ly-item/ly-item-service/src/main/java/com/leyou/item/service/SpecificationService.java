package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.mapper.SpecificationMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.pojo.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class SpecificationService {
    @Autowired
    private SpecGroupMapper specGroupMapper;

    @Autowired
    private SpecParamMapper specParamMapper;

    @Autowired
    private SpecificationMapper specificationMapper;

    public List<SpecGroup> querySpecGroupByCid(Long cid) {
        //查询条件
        SpecGroup group = new SpecGroup();
        group.setCid(cid);
        //查询
        List<SpecGroup> list = specGroupMapper.select(group);
        if(CollectionUtils.isEmpty(list)){
            //没查到
            throw new LyException(ExceptionEnum.SPEC_GROUP_NOT_FOUND);
        }

        return list;
    }

    public List<SpecParam> querySpecParamList(Long gid, Long cid, Boolean searching) {
        SpecParam param=new SpecParam();
        param.setGroupId(gid);
        param.setCid(cid);
        param.setSearching(searching);

        List<SpecParam> list = specParamMapper.select(param);
        if(CollectionUtils.isEmpty(list)){
            throw  new LyException(ExceptionEnum.SPEC_PARAM_NOT_FOUND);
        }
        return list;
    }

    public List<SpecGroup> queryListByCid(Long cid) {
        // 查询规格组
        List<SpecGroup> specGroups = querySpecGroupByCid(cid);
        // 查询当前分类下的参数
        List<SpecParam> specParams = querySpecParamList(null, cid,  null);
        //先把规格参数变成map，map的key是规格组id,map的值是组下的所有参数
        Map<Long, List<SpecParam>> map = new HashMap<>();

        for (SpecParam param : specParams) {
            if (!map.containsKey(param.getGroupId())) {
                //这个组id在map中不存在，新增一个list
                map.put(param.getGroupId(), new ArrayList<>());
            }
            map.get(param.getGroupId()).add(param);

        }
        //填充param到group
        for (SpecGroup specGroup : specGroups) {
            specGroup.setParams(map.get(specGroup.getId()));
        }

        return specGroups;
    }

    /**
     * 根据id查询规格参数模板
     * @param id
     * @return
     */
//    public Specification queryById(Long id) {
//        return this.specificationMapper.selectByPrimaryKey(id);
//    }

    /**
     * 保存规格参数模板
     * @param specification
     */
    public void saveSpecification(Specification specification) {
        this.specificationMapper.insert(specification);
    }

    /**
     * 修改规格参数模板
     * @param specification
     */
    public void updateSpecification(Specification specification) {
        this.specificationMapper.updateByPrimaryKeySelective(specification);
    }

    /**
     * 删除规格参数模板
     * @param specification
     */
    public void deleteSpecification(Specification specification) {
        this.specificationMapper.deleteByPrimaryKey(specification);
    }

}
