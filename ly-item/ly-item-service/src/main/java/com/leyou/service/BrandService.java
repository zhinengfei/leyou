package com.leyou.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.mapper.BrandMapper;
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

    public PageResult<Brand> queryBrandByPage(Integer page, Integer rows, String sortBy, Boolean desc, String key) {
        //分页
        PageHelper.startPage(page,rows);
        //过滤
        Example example = new Example(Brand.class);
        if(StringUtils.isNotBlank(key)){
            //过滤条件
            example.createCriteria().orLike("name","%"+key+"%").orEqualTo("letter",key.toUpperCase());
        }
        //排序
        if(StringUtils.isNotBlank(sortBy)){
            String orderByClause = sortBy + (desc ? " DESC" : " ASC");
            example.setOrderByClause(orderByClause);
        }
        //查询
        List<Brand> brandList = brandMapper.selectByExample(example);

        //解析结果
        if(CollectionUtils.isEmpty(brandList)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        PageInfo<Brand> pageInfo = new PageInfo<>(brandList);

        return new PageResult<>(pageInfo.getTotal(),brandList);
    }

    @Transactional
    public void saveBrand(Brand brand, List<Long> ids) {
        //新增品牌
        int count = brandMapper.insert(brand);
        if(count != 1){
            throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
        }

        //新增中间表
        try{
            ids.stream().forEach(id -> brandMapper.insertCategoryBrand(id,brand.getId()));
        }catch (Exception e){
            throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
        }

    }

    public Brand queryBrandById(Long brandId){
        Brand brand = brandMapper.selectByPrimaryKey(brandId);
        if(brand == null){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brand;
    }

    public void updateBrand(Brand brand){
        int count = brandMapper.updateByPrimaryKey(brand);
        if(count == 0){
            //跟新失败
            throw new LyException(ExceptionEnum.UPDATE_BRAND_FAIL);
        }

    }

    public void deleteBrandById(Long id){
        int count = brandMapper.deleteByPrimaryKey(id);
        if(count == 0){
            //跟新失败
            throw new LyException(ExceptionEnum.DELETE_BRAND_FAIL);
        }
    }

    public List<Brand> queryByCid(Long cid) {
        List<Brand> brandList = brandMapper.queryByCategoryId(cid);
        if(CollectionUtils.isEmpty(brandList)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brandList;
    }

    public List<Brand> queryBrandByIds(List<Long> ids) {
        List<Brand> brandList = brandMapper.selectByIdList(ids);
        if(CollectionUtils.isEmpty(brandList)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brandList;
    }
}
