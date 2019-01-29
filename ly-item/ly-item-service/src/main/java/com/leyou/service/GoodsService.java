package com.leyou.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.LyItemApplication;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.*;
import com.leyou.mapper.SkuMapper;
import com.leyou.mapper.SpuDetailMapper;
import com.leyou.mapper.SpuMapper;
import com.leyou.mapper.StockMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    public PageResult<Spu> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key) {

        //分页
        PageHelper.startPage(page,rows);
        //过滤
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        //过滤key
        if(StringUtils.isNotBlank(key)){
            criteria.andLike("title","%"+key+"%");
        }
        //过滤上架和下架
        if(saleable != null){
            criteria.andEqualTo("saleable",saleable);
        }
        //默认排序
        example.setOrderByClause("last_update_time DESC");

        List<Spu> spus = spuMapper.selectByExample(example);

        //判断
        if(CollectionUtils.isEmpty(spus)){
            throw  new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }

        //解析分类的品牌和名称
        loadCategoryAndBrandName(spus);

        PageInfo<Spu> info = new PageInfo<>(spus);
        return new PageResult<>(info.getTotal(),spus);
    }

    private void loadCategoryAndBrandName(List<Spu> spus) {
         for(Spu spu : spus){
             List<String> list = categoryService.queryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3())).stream().map(Category::getName).collect(Collectors.toList());
             spu.setCname(StringUtils.join(list,"/"));
             spu.setBname(brandService.queryBrandById(spu.getBrandId()).getName());
         }
    }

    @Transactional
    public void saveGoods(Spu spu) {
        //新增spu
        spu.setId(null);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        spu.setSaleable(true);
        spu.setValid(false);
        int count = spuMapper.insert(spu);
        if(count == 0){
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
        //新增detail
        SpuDetail spuDetail = spu.getSpuDetail();
        spuDetail.setSpuId(spu.getId());
        spuDetailMapper.insert(spuDetail);
        //定义库存集合
        List<Stock> stocks = new ArrayList<>();
        //新增sku
        List<Sku> skus = spu.getSkus();
        for(Sku sku : skus){
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            sku.setSpuId(spu.getId());

            count = skuMapper.insert(sku);
            if(count == 0){
                throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
            }

            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());

            stocks.add(stock);
        }
        count = stockMapper.insertList(stocks);
        if(count == 0){
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }

        //发送mq消息
        amqpTemplate.convertAndSend("item.insert",spu.getId());
    }



    public SpuDetail querySpuDetailById(Long id) {
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(id);
        if(spuDetail == null){
            throw  new LyException(ExceptionEnum.SPU_DETAIL_NOT_FOUND);
        }
        return spuDetail;
    }

    public List<Sku> selectSkuBySpuId(Long id) {
        Sku sku = new Sku();
        sku.setSpuId(id);
        List<Sku> skus = skuMapper.select(sku);
        if(CollectionUtils.isEmpty(skus)){
            throw new LyException(ExceptionEnum.SKU_NOT_FOUND);
        }

        //查询库存
        for(Sku sku1 : skus){
            Stock stock = stockMapper.selectByPrimaryKey(sku1.getId());
            if(stock == null){
                throw new LyException(ExceptionEnum.SKU_STOCK_NOT_FOUND);
            }
            sku1.setStock(stock.getStock());
        }

        return skus;
    }

    private void saveSkuAndStock(Spu spu) {
        List<Stock> list = new ArrayList<>();
        List<Sku> skus = spu.getSkus();
        for (Sku sku : skus) {
            // 保存sku
            sku.setSpuId(spu.getId());
            // 初始化时间
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            int count = skuMapper.insert(sku);
            if (count != 1) {
                throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
            }
            System.out.println(3);
            // 保存库存信息
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            list.add(stock);
        }
        // 批量新增库存
        int count1 = stockMapper.insertList(list);
        if (count1 == 0) {
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
        System.out.println(4);
    }

    @Transactional
    public void updateGoods(Spu spu) {
        if(spu.getId() == null){
            throw new LyException(ExceptionEnum.GOODS_ID_CANNOT_BE_NULL);
        }
        Sku sku = new Sku();
        sku.setSpuId(spu.getId());
        // 查询以前sku
        List<Sku> skus = skuMapper.select(sku);
        if(!CollectionUtils.isEmpty(skus)){
            //删除sku
            skuMapper.delete(sku);
            //删除stock
            List<Long> ids = skus.stream().map(Sku::getSpuId).collect(Collectors.toList());
            stockMapper.deleteByIdList(ids);
        }
        //修改spu
        spu.setValid(null);
        spu.setSaleable(null);
        spu.setLastUpdateTime(new Date());
        spu.setCreateTime(null);

        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if(count == 0){
            throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }
        System.out.println(1);
        //修改detail
        count = spuDetailMapper.updateByPrimaryKeySelective(spu.getSpuDetail());
        if(count == 0){
            throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }
        System.out.println(2);
        //新增sku和stock
        saveSkuAndStock(spu);

        //发送mq消息
        amqpTemplate.convertAndSend("item.update",spu.getId());
    }


    public Spu querySpuById(Long id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if(spu == null){
            throw new LyException(ExceptionEnum.SPU_NOT_FOUND);
        }
        //查询sku
        spu.setSkus(selectSkuBySpuId(id));
        //查询detail
        spu.setSpuDetail(querySpuDetailById(id));
        return spu;
    }

    public List<Sku> querySkuByIds(List<Long> ids) {
        List<Sku> skus = skuMapper.selectByIdList(ids);
        if(CollectionUtils.isEmpty(skus)){
            throw new LyException(ExceptionEnum.SKU_STOCK_NOT_FOUND);
        }

        List<Sku> skuList = new ArrayList<>();
        //查询库存
        for(Sku sku1 : skus){
            Stock stock = stockMapper.selectByPrimaryKey(sku1.getId());
            if(stock == null){
                throw new LyException(ExceptionEnum.SKU_STOCK_NOT_FOUND);
            }
            sku1.setStock(stock.getStock());
            skuList.add(sku1);
        }

        return skuList;
    }
}
