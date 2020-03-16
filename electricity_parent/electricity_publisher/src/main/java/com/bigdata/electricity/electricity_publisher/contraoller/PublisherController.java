package com.bigdata.electricity.electricity_publisher.contraoller;

import com.alibaba.fastjson.JSON;
import com.bigdata.electricity.electricity_publisher.bean.CityName;
import com.bigdata.electricity.electricity_publisher.bean.Option;
import com.bigdata.electricity.electricity_publisher.bean.OptionGroup;
import com.bigdata.electricity.electricity_publisher.bean.SaleDetailInfo;
import com.bigdata.electricity.electricity_publisher.service.PublisherService;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class PublisherController {
    @Autowired
    PublisherService publisherService;

    @GetMapping("realtime-total")
    public String getTotal(@RequestParam("date") String date) {
        List<Map> totalList = new ArrayList<>();
        Map dauMap = new HashMap();
        dauMap.put("id", "dau");
        dauMap.put("name", "新增日活");
        Integer dauTotal = publisherService.getDauTotal(date);
        dauMap.put("value", dauTotal);
        totalList.add(dauMap);

        Map orderAmountMap = new HashMap();
        orderAmountMap.put("id", "orderAmount");
        orderAmountMap.put("name", "新增交易额");
        Double orderAmount = publisherService.getOrderAmount(date);
        orderAmountMap.put("value", orderAmount);
        totalList.add(orderAmountMap);


        return JSON.toJSONString(totalList);
    }


    @GetMapping("realtime-hour")
    public String getHourTotal(@RequestParam("id") String id, @RequestParam("date") String today) {
        if ("dau".equals(id)) {
            //今天
            Map dauHourTDMap = publisherService.getDauHourMap(today);
            //求昨天分时明细
            String yesterday = getYesterday(today);
            Map dauHourYDMap = publisherService.getDauHourMap(yesterday);

            Map hourMap = new HashMap();
            hourMap.put("today", dauHourTDMap);
            hourMap.put("yesterday", dauHourYDMap);
            return JSON.toJSONString(hourMap);
        } else if ("orderAmount".equals(id)) {
            //今天
            Map orderAmountHourTdMap = publisherService.getOrderAmountHourMap(today);
            //求昨天分时明细
            String yesterday = getYesterday(today);
            Map orderAmountHourYDMAP = publisherService.getOrderAmountHourMap(yesterday);

            Map hourMap = new HashMap();
            hourMap.put("today", orderAmountHourTdMap);
            hourMap.put("yesterday", orderAmountHourYDMAP);
            return JSON.toJSONString(hourMap);
        }

        return null;
    }

    private String getYesterday(String today) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String yesterday = "";
        try {
            Date todayDate = simpleDateFormat.parse(today);
            Date yesterdayDate = DateUtils.addDays(todayDate, -1);
            yesterday = simpleDateFormat.format(yesterdayDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return yesterday;
    }

    // http://localhost:8070/sale_detail?date=2020-02-03&&startpage=1&size=5&keyword=手机小米
    @GetMapping("sale_detail")
    public String getSaleDetail(@RequestParam("date") String date,
                                @RequestParam("startpage") int pageNo,
                                @RequestParam("size") int pageSize,
                                @RequestParam("keyword") String keyword) {
        Map saleDetailMapWithGender = publisherService.getSaleDetailMap(date, keyword, pageNo, pageSize, "user_gender", 2);
        Integer total = (Integer) saleDetailMapWithGender.get("total");
        List<Map> detailList = (List<Map>) saleDetailMapWithGender.get("detail");
        Map aggsMapGender = (Map) saleDetailMapWithGender.get("aggsMap");

        Long femaleCount = (Long) aggsMapGender.getOrDefault("F", 0);
        Long maleCount = (Long) aggsMapGender.getOrDefault("M", 0);

        Double maleRatio = Math.round(maleCount * 1000D / total) / 10D;
        Double femaleRatio = Math.round(femaleCount * 1000D / total) / 10D;
        List<Option> optionListGender = new ArrayList<>();
        optionListGender.add(new Option("男", maleRatio));
        optionListGender.add(new Option("女", femaleRatio));

        OptionGroup optionGroupGender = new OptionGroup("性别占比", optionListGender);

        Map saleDetailMapWithAge = publisherService.getSaleDetailMap(date, keyword, pageNo, pageSize, "user_age", 100);
        Map aggsMapAge = (Map) saleDetailMapWithAge.get("aggsMap");

        Long age_20count = 0L;
        Long age20_30count = 0L;
        Long age30_count = 0L;


        for (Object o : aggsMapAge.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            String age = (String) entry.getKey();
            Long ageCount = (Long) entry.getValue();
            if (Integer.parseInt(age) < 20) {
                age_20count += ageCount;
            } else if (Integer.parseInt(age) >= 20 && Integer.parseInt(age) <= 30) {
                age20_30count += ageCount;
            } else {
                age30_count += ageCount;
            }
        }
        //各年龄段的占比
        Double age_20Ratio = Math.round(age_20count * 1000D / total) / 10D;
        Double age20_30Ratio = Math.round(age20_30count * 1000D / total) / 10D;
        Double age30_Ratio = Math.round(age30_count * 1000D / total) / 10D;

        List<Option> optionListAge = new ArrayList<>();
        optionListAge.add(new Option("20岁以下", age_20Ratio));
        optionListAge.add(new Option("20岁-30岁", age20_30Ratio));
        optionListAge.add(new Option("30岁以上", age30_Ratio));
        OptionGroup optionGroupAge = new OptionGroup("年龄占比", optionListAge);


        List<OptionGroup> optionGroups = new ArrayList<>();
        optionGroups.add(optionGroupAge);
        optionGroups.add(optionGroupGender);


        SaleDetailInfo saleDetailInfo = new SaleDetailInfo(total, optionGroups, detailList);

        return JSON.toJSONString(saleDetailInfo);
    }


    @GetMapping("city_name")
    public String getCityName() {
        Map orderAmount1 = publisherService.getCityName();
        Iterator<Map.Entry<String, Long>> iterator = orderAmount1.entrySet().iterator();
        ArrayList<CityName> productArrayList = new ArrayList<CityName>();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            String key = entry.getKey();
            Long value = entry.getValue();
            CityName product1 = new CityName();
            product1.setName(key);
            product1.setValue(value);
            productArrayList.add(product1);
        }
        return JSON.toJSONString(productArrayList);
    }
}
