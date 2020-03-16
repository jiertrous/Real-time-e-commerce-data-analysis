package com.electricity.canal.app;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.electricity.hander.CanalHander;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.net.InetSocketAddress;
import java.util.List;
public class CanalApp {

    public static void main(String[] args) {
        //创建连接器
        CanalConnector canalConnector = CanalConnectors.newSingleConnector(new InetSocketAddress("master", 11111), "example", "", "");

        while(true){
            //连接、订阅 、抓取数据
            canalConnector.connect();
            canalConnector.subscribe("electricity.order_info");
            Message message = canalConnector.get(100);
            int size = message.getEntries().size();
            if(size==0){
                try {
                    System.out.println("没有数据，休息一会");
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }else{
                for (CanalEntry.Entry entry : message.getEntries()) {
                    //判断事件类型  只处理 行变化业务
                    if(entry.getEntryType().equals(CanalEntry.EntryType.ROWDATA)){
                        //把数据集进行反序列化
                        ByteString storeValue = entry.getStoreValue();
                        CanalEntry.RowChange rowChange=null;

                        try {
                            rowChange = CanalEntry.RowChange.parseFrom(storeValue);
                        } catch (InvalidProtocolBufferException e) { // 谷歌序列化工具
                            e.printStackTrace();
                        }
                        //获得行集
                        List<CanalEntry.RowData> rowDatasList = rowChange.getRowDatasList();
                        CanalEntry.EventType  eventType = rowChange.getEventType();   //操作类型
                        String tableName = entry.getHeader().getTableName();//表名

                        CanalHander .handle(tableName,eventType,rowDatasList);
                    }




                }




            }

        }


    }
}
