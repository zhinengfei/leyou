package com.leyou.web;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/spec")
public class SpecificationController {

    @Autowired
    private SpecificationService specificationService;

    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupsByCid(@PathVariable("cid") Long cid){
        List<SpecGroup> specGroupList = specificationService.queryGroupsByCid(cid);
        return ResponseEntity.ok(specGroupList);
    }

    /**
     * 查询品牌spu参数的集合
     * @param gid
     * @param cid
     * @param searching
     * @return
     */
    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> querySpecParamByGroupId(@RequestParam(value = "gid",required = false) Long gid ,
                                                                   @RequestParam(value = "cid",required = false) Long cid,
                                                                   @RequestParam(value = "searching",required = false)Boolean searching){
            return ResponseEntity.ok(specificationService.queryParamByGroupId(gid,cid,searching));

    }

    /**
     * 根据分类查询规格组
     * @param cid
     * @return
     */
    @GetMapping("group")
    public ResponseEntity<List<SpecGroup>> queryListByCid(@RequestParam("cid") Long cid){
        return ResponseEntity.ok(specificationService.queryListByCid(cid));
    }

}
