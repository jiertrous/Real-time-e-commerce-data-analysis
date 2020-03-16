package com.electricity.hander;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.electricity.common.constant.ElecConstants;
import com.electricity.util.MyKafkaSender;
import com.google.common.base.CaseFormat;

import java.util.List;

public class CanalHander {


    public  static  void handle(String tableName , CanalEntry.EventType eventType, List<CanalEntry.RowData> rowDatasList){

        if("order_info".equals(tableName)&& CanalEntry.EventType.INSERT.equals(eventType)){
            //下单操作
            for (CanalEntry.RowData rowData : rowDatasList) {  //行集展开
                List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
                JSONObject jsonObject=new JSONObject();
                for (CanalEntry.Column column : afterColumnsList) {  //列集展开
                    System.out.println(column.getName()+":::"+column.getValue());
                    // 下划线转驼峰
                    String propertyName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, column.getName());
                    jsonObject.put(propertyName,column.getValue());
                    }
                MyKafkaSender.send(ElecConstants .KAFKA_TOPIC_ORDER,jsonObject.toJSONString());
            }

        }


    }

}
