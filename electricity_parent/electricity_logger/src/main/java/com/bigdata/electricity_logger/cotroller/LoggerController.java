package com.bigdata.electricity_logger.cotroller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.electricity.common.constant.ElecConstants;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;


@RestController
public class LoggerController {

    @Autowired
    KafkaTemplate<String,String> kafkaTemplate;

    private static final  org.slf4j.Logger logger = LoggerFactory.getLogger(LoggerController.class) ;

    @PostMapping("/log")
//    @ResponseBody
    public String dolog(@RequestParam("log") String logJson){

        // 补时间戳，客户时间戳可能不准，用自己服务器的
         JSONObject jsonObject = JSON.parseObject(logJson);
         jsonObject.put("ts",System.currentTimeMillis());

        // 保存到logfile log4j
        logger.info(jsonObject.toJSONString());

        // 发送kafka
        if ("startup".equals(jsonObject.getString("type"))){
            kafkaTemplate.send(ElecConstants.KAFKA_TOPIC_STARTUP,jsonObject.toString());
        }else{
            kafkaTemplate.send(ElecConstants.KAFKA_TOPIC_EVENT,jsonObject.toString());
        }





        System.out.println(logJson);
        return "success";
    }
}
