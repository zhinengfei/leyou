package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.utils.NumberUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.UnmappedTerms;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SearchService {

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private GoodsRepository repository;

    @Autowired
    private ElasticsearchTemplate template;

    public Goods buildGoods(Spu spu){
        //查询分类
        List<Category> categoryList = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        if(CollectionUtils.isEmpty(categoryList)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        //查询品牌
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        if(brand == null){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        //搜索字段
        String all = spu.getTitle() + org.apache.commons.lang3.StringUtils.join(categoryList," ")+brand.getName();

        //查询sku
        List<Sku> skus = goodsClient.querySkuBySpuId(spu.getId());
        if(CollectionUtils.isEmpty(skus)){
            throw new LyException(ExceptionEnum.SKU_STOCK_NOT_FOUND);
        }
        //对sku进行处理
        List<Map<String,Object>> skuList = new ArrayList<>();
        //价格集合
        Set<Long> prices = new HashSet<>();
        for(Sku sku : skus){
            Map<String,Object> map = new HashMap<>();
            map.put("id",sku.getId());
            map.put("title",sku.getTitle());
            map.put("price",sku.getPrice());
            map.put("image",StringUtils.substringBefore(sku.getImages(), ","));
            skuList.add(map);
            prices.add(sku.getPrice());
        }
        //规格参数
        List<SpecParam> params = specificationClient.querySpecParamByGroupId(null, spu.getCid3(), true);
        //查询商品详情
        SpuDetail spuDetail = goodsClient.querySpuDetailById(spu.getId());
        //获取通用规格参数
        Map<Long, String> genericSpec = JsonUtils.parseMap(spuDetail.getGenericSpec(), Long.class, String.class);
        //获取特有规格参数
        Map<Long, List<String>> specialSpec = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>() {
        });
        //规格参数,key是规格参数的名字,值是值
        Map<String,Object> specs = new HashMap<>();
        for (SpecParam param : params) {
            //key是规格参数的名称
            String key = param.getName();
            Object value = "";

            if (param.getGeneric()) {
                //参数是通用属性，通过规格参数的ID从商品详情存储的规格参数中查出值
                value = genericSpec.get(param.getId());
                if (param.getNumeric()) {
                    //参数是数值类型，处理成段，方便后期对数值类型进行范围过滤
                    value = chooseSegment(value.toString(), param);
                }
            } else {
                //参数不是通用类型
                value = specialSpec.get(param.getId());
            }
            //存入map
            specs.put(key, value);
        }
        //构建goods对象
        Goods goods = new Goods();
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setId(spu.getId());

        //搜索字段,包括标题 分类 品牌 规格等
        goods.setAll(all);
        goods.setSkus(JsonUtils.serialize(skus));
        goods.setPrice(prices);
        goods.setSpecs(specs);
        goods.setSubTitle(spu.getSubTitle());
        return goods;
    }

    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    public PageResult<Goods> search(SearchRequest request) {
        Integer page = request.getPage()-1;
        Integer size = request.getSize();

        String key = request.getKey();
        if(StringUtils.isBlank(key)) {
            return null;
        }

        //创建查询构建器
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        //结果过滤
        builder.withSourceFilter(new FetchSourceFilter(new String[]{"id","subTitle","skus"},null));
        //分页
        builder.withPageable(PageRequest.of(page,size));
        //过滤
        QueryBuilder basicQuery = buildBasicQuery(request);
        builder.withQuery(basicQuery);

        //聚合分类和品牌
        String categoryAggName = "category_agg";
        builder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));

        String brandAggName = "brand_agg";
        builder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));
        // 查询
        AggregatedPage<Goods> result = template.queryForPage(builder.build(), Goods.class);

        //解析结果
        List<Goods> goodsList = result.getContent();
        int totalPage = result.getTotalPages();
        long total = result.getTotalElements();

        //解析聚合结果
        Aggregations aggs = result.getAggregations();
        List<Category> categoryList = parseCategoryAggs(aggs.get(categoryAggName));
        List<Brand> brandList = parseBrandAgg(aggs.get(brandAggName));

        List<Map<String,Object>> specs = null;
        if(categoryList != null && categoryList.size() == 1){
            specs = buildSpecificationAgg(categoryList.get(0).getId(),basicQuery);
        }

        return new SearchResult(total,totalPage,goodsList,categoryList,brandList,specs);
    }

    private QueryBuilder buildBasicQuery(SearchRequest request) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        // 基本查询条件
        queryBuilder.must(QueryBuilders.matchQuery("all", request.getKey()).operator(Operator.AND));
        // 过滤条件构建器
        BoolQueryBuilder filterQueryBuilder = QueryBuilders.boolQuery();
        // 整理过滤条件
        Map<String, String> filter = request.getFilter();
        for (Map.Entry<String, String> entry : filter.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            // 商品分类和品牌要特殊处理
            if (key != "cid3" && key != "brandId") {
                key = "specs." + key + ".keyword";
            }
            // 字符串类型，进行term查询
            filterQueryBuilder.must(QueryBuilders.termQuery(key, value));
        }
        // 添加过滤条件
        queryBuilder.filter(filterQueryBuilder);
        return queryBuilder;
    }

    private List<Map<String, Object>> buildSpecificationAgg(Long cid, QueryBuilder query) {
        try{
            // 根据分类查询规格
            List<SpecParam> params = this.specificationClient.querySpecParamByGroupId(null, cid, true);
            // 创建集合，保存规格过滤条件
            List<Map<String, Object>> specs = new ArrayList<>();

            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
            queryBuilder.withQuery(query);

            // 聚合规格参数
            for(SpecParam param : params){
                String name = param.getName();
                queryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs."+name+".keyword"));
            }
            // 查询
            AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);
            // 解析聚合结果
            Aggregations aggs = result.getAggregations();
            for(SpecParam param1 : params){
                String name = param1.getName();
                StringTerms terms = aggs.get(name);
                //准备map
                Map<String,Object> map = new HashMap<>();
                map.put("k",name);
                map.put("options",terms.getBuckets().stream().map(b -> b.getKeyAsString()).collect(Collectors.toList()));
                specs.add(map);
            }
            return specs;
        }catch (Exception e){
            log.error("[搜索服务]查询分类异常",e);
            return null;
        }

    }

    private List<Brand> parseBrandAgg(LongTerms terms) {
        try {
            List<Long> ids = terms.getBuckets().stream().map(b -> b.getKeyAsNumber().longValue()).collect(Collectors.toList());
            return brandClient.queryBrandByIds(ids);
        }catch (Exception e){
            log.error("[搜索服务]查询品牌异常",e);
            return null;
        }
    }

    private List<Category> parseCategoryAggs(LongTerms terms) {
        try {
            List<Long> ids = terms.getBuckets().stream().map(b -> b.getKeyAsNumber().longValue()).collect(Collectors.toList());
            List<Category> categoryList = categoryClient.queryCategoryByIds(ids);
            return categoryList;
        }catch (Exception e){
            log.error("[搜索服务]查询分类异常",e);
            return null;
        }
    }

    public void createOrUpdateIndex(Long spuId) {
        //查询spu
        Spu spu = goodsClient.querySpuById(spuId);
        //构建goods
        Goods goods = buildGoods(spu);
        //存入索引库
        repository.save(goods);
    }

    public void deleteIndex(Long spuId) {
        //查询spu
        Spu spu = goodsClient.querySpuById(spuId);
        //构建goods
        Goods goods = buildGoods(spu);
        //存入索引库
        repository.save(goods);
    }
}
