package com.leyou.search.pojo;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
public class SearchResult extends PageResult {

    private List<Category> categoryList;

    private List<Brand> brands;

    private List<Map<String,Object>> specs;

    public SearchResult(){}

    public SearchResult(Long total,Integer totalPage,List<Goods> items,List<Category> categories,List<Brand> brands,List<Map<String,Object>> specs){
        super(total,totalPage,items);
        this.categoryList = categories;
        this.brands = brands;
        this.specs = specs;
    }
}
