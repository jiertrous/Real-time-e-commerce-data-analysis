package com.bigdata.electricity.electricity_publisher.service.impl;

import com.alibaba.fastjson.JSON;
import com.electricity.areatop.app.AreaTop;
import com.electricity.common.constant.ElecConstants;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.apache.spark.SparkConf;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.sum.SumAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.Tuple2;
import scala.math.BigInt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PublisherServiceImpl implements com.bigdata.electricity.electricity_publisher.service.PublisherService {

    @Autowired
    JestClient jestClient;

    @Override
    public Integer getDauTotal(String date) {
        String query = "{\n" +
                "  \"query\": {\n" +
                "    \"bool\": {\n" +
                "      \"filter\": {\n" +
                "        \"term\": {\n" +
                "          \"logDate\": \"2019-12-03\"\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.filter(new TermQueryBuilder("logDate", date));
        searchSourceBuilder.query(boolQueryBuilder);

        System.out.println(searchSourceBuilder.toString());

        Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex(ElecConstants.ES_INDEX_DAU).addType("_doc").build();
        Integer total = 0;
        try {
            SearchResult searchResult = jestClient.execute(search);
            total = Math.toIntExact(searchResult.getTotal());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return total;
    }

    @Override
    public Map getDauHourMap(String date) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //过滤
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.filter(new TermQueryBuilder("logDate", date));
        searchSourceBuilder.query(boolQueryBuilder);
        //聚合
        //AggregationBuilders.terms("groupby_logHour").field("logHour").size(24);
        searchSourceBuilder.aggregation(AggregationBuilders.terms("groupby_logHour").field("logHour").size(24));

        Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex(ElecConstants.ES_INDEX_DAU).addType("_doc").build();

        Map dauHourMap = new HashMap();
        try {
            SearchResult searchResult = jestClient.execute(search);
            List<TermsAggregation.Entry> buckets = searchResult.getAggregations().getTermsAggregation("groupby_logHour").getBuckets();
            for (TermsAggregation.Entry bucket : buckets) {
                String key = bucket.getKey();
                Long count = bucket.getCount();
                dauHourMap.put(key, count);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return dauHourMap;
    }

    @Override
    public Double getOrderAmount(String date) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //过滤
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.filter(new TermQueryBuilder("createDate", date));
        searchSourceBuilder.query(boolQueryBuilder);
        //聚合
        SumAggregationBuilder aggsBuilder = AggregationBuilders.sum("sum_totalamount").field("totalAmount");
        searchSourceBuilder.aggregation(aggsBuilder);


        Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex(ElecConstants.ES_INDEX_ORDER).addType("_doc").build();
        Double sum_totalamount = 0D;
        try {
            SearchResult searchResult = jestClient.execute(search);
            sum_totalamount = searchResult.getAggregations().getSumAggregation("sum_totalamount").getSum();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sum_totalamount;
    }

    @Override
    public Map getOrderAmountHourMap(String date) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //过滤
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.filter(new TermQueryBuilder("createDate", date));
        searchSourceBuilder.query(boolQueryBuilder);

        //聚合
        TermsAggregationBuilder termsBuilder = AggregationBuilders.terms("groupby_createHour").field("createHour").size(24);
        SumAggregationBuilder sumBuilder = AggregationBuilders.sum("sum_totalamount").field("totalAmount");

        //子聚合
        termsBuilder.subAggregation(sumBuilder);
        searchSourceBuilder.aggregation(termsBuilder);


        Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex(ElecConstants.ES_INDEX_ORDER).addType("_doc").build();
        Map<String, Double> hourMap = new HashMap<>();
        try {
            SearchResult searchResult = jestClient.execute(search);
            List<TermsAggregation.Entry> buckets = searchResult.getAggregations().getTermsAggregation("groupby_createHour").getBuckets();
            for (TermsAggregation.Entry bucket : buckets) {
                Double hourAmount = bucket.getSumAggregation("sum_totalamount").getSum();
                String hourkey = bucket.getKey();
                hourMap.put(hourkey, hourAmount);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hourMap;
    }

    @Override
    public Map getSaleDetailMap(String date, String keyword, int pageNo, int pageSize, String aggsFieldName, int aggsSize) {
        Integer total=0;
        List<Map> detailList=new ArrayList<>();
        Map<String ,Long> aggsMap=new HashMap<>();

        Map saleMap=new HashMap();

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //过滤
        boolQueryBuilder.filter(new TermQueryBuilder("dt",date));
        //全文匹配
        boolQueryBuilder.must(new MatchQueryBuilder("sku_name",keyword).operator(Operator.AND));


        searchSourceBuilder.query(boolQueryBuilder);

        //聚合
        TermsAggregationBuilder termsBuilder = AggregationBuilders.terms("groupby_" + aggsFieldName).field(aggsFieldName).size(aggsSize);
        searchSourceBuilder.aggregation(termsBuilder);

        //分页
        searchSourceBuilder.from((pageNo-1)*pageSize);
        searchSourceBuilder.size(pageSize);

        System.out.println(searchSourceBuilder.toString());

        Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex(ElecConstants.ES_INDEX_SALE).addType("_doc").build();
        try {
            SearchResult searchResult = jestClient.execute(search);
            total = Math.toIntExact(searchResult.getTotal());
            //明细
            List<SearchResult.Hit<Map, Void>> hits = searchResult.getHits(Map.class);
            for (SearchResult.Hit<Map, Void> hit : hits) {
                detailList.add(hit.source);
            }
            //取聚合结果
            List<TermsAggregation.Entry> buckets = searchResult.getAggregations().getTermsAggregation("groupby_" + aggsFieldName).getBuckets();
            for (TermsAggregation.Entry bucket : buckets) {
                aggsMap.put( bucket.getKey(),bucket.getCount());
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        saleMap.put("total",total);
        saleMap.put("detail",detailList);
        saleMap.put("aggsMap",aggsMap);


        return saleMap;
    }

    @Override
    public Map getCityName() {
        String query = " GET electricity_city_name/_search\n" +
                " {\n" +
                "  \"aggs\": {\n" +
                "    \"groupby_city\": {\n" +
                "      \"terms\": {\n" +
                "        \"field\": \"sku_tm_name\" \n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.aggregation(AggregationBuilders.terms("groupby_city").field("city_name")).size(9);
        Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex(ElecConstants.ES_INDEX_CITY).addType("_doc").build();

        Map dauHourMap1 = new HashMap();
        try {
            SearchResult searchResult = jestClient.execute(search);
            List<TermsAggregation.Entry> buckets = searchResult.getAggregations().getTermsAggregation("groupby_city").getBuckets();
            for (TermsAggregation.Entry bucket : buckets) {
                String key = bucket.getKey();
                Long count = bucket.getCount();
                dauHourMap1.put(key,count);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(dauHourMap1);
        //{apple=91, 北纯=170, 苹果=1, 华为=1, 一加=1, 传音=1, tcl=69, 荣耀=242, 小米=159, 三星=1}
        return dauHourMap1;
    }
}
