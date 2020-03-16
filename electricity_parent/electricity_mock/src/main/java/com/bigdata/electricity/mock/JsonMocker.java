package com.bigdata.electricity.mock;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bigdata.electricity.mock.util.*;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;



public class JsonMocker {

    int startupNum = 100000;
    int eventNum = 200000;

    RandomDate logDateUtil = null;


    RandomOpt[] osOpts = {new RandomOpt("ios", 3), new RandomOpt("andriod", 7)};
    RandomOptionGroup<String> osOptionGroup = new RandomOptionGroup(osOpts);
    Date startTime = null;
    Date endTime = null;

    RandomOpt[] areaOpts = {new RandomOpt("beijing", 10),
            new RandomOpt("shanghai", 10), new RandomOpt("guangdong", 20), new RandomOpt("hebei", 5),
            new RandomOpt("heilongjiang", 5), new RandomOpt("shandong", 5), new RandomOpt("tianjin", 5),
            new RandomOpt("shan3xi", 5), new RandomOpt("shan1xi", 5), new RandomOpt("sichuan", 5)
    };
    RandomOptionGroup<String> areaOptionGroup = new RandomOptionGroup(areaOpts);

    String appId = "User10001";

    RandomOpt[] vsOpts = {new RandomOpt("1.2.0", 50), new RandomOpt("1.1.2", 15),
            new RandomOpt("1.1.3", 30),
            new RandomOpt("1.1.1", 5)
    };

    RandomOptionGroup<String> vsOptionGroup = new RandomOptionGroup(vsOpts);

    RandomOpt[] eventOpts = {new RandomOpt("addFavor", 10), new RandomOpt("addComment", 30),
            new RandomOpt("addCart", 20), new RandomOpt("clickItem", 40)
    };

    RandomOptionGroup<String> eventOptionGroup = new RandomOptionGroup(eventOpts);

    RandomOpt[] channelOpts = {new RandomOpt("xiaomi", 10), new RandomOpt("huawei", 20),
            new RandomOpt("wandoujia", 30), new RandomOpt("360", 20), new RandomOpt("tencent", 20)
            , new RandomOpt("baidu", 10), new RandomOpt("website", 10)
    };

    RandomOptionGroup<String> channelOptionGroup = new RandomOptionGroup(channelOpts);

    RandomOpt[] quitOpts = {new RandomOpt(true, 20), new RandomOpt(false, 80)};

    RandomOptionGroup<Boolean> isQuitGroup = new RandomOptionGroup(quitOpts);

    public JsonMocker() {

    }

    public JsonMocker(String startTimeString, String endTimeString, int startupNum, int eventNum) {
        try {
            startTime = new SimpleDateFormat("yyyy-MM-dd").parse(startTimeString);
            endTime = new SimpleDateFormat("yyyy-MM-dd").parse(endTimeString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        logDateUtil = new RandomDate(startTime, endTime, startupNum + eventNum);
    }

    String initEventLog(String startLogJson) {
            /*`type` string   COMMENT '日志类型',
             `mid` string COMMENT '设备唯一 表示',
            `uid` string COMMENT '用户标识',
            `os` string COMMENT '操作系统',
            `appid` string COMMENT '应用id',
            `area` string COMMENT '地区' ,
            `evid` string COMMENT '事件id',
            `pgid` string COMMENT '当前页',
            `npgid` string COMMENT '跳转页',
            `itemid` string COMMENT '商品编号',
            `ts` bigint COMMENT '时间',*/

        JSONObject startLog = JSON.parseObject(startLogJson);
        String mid = startLog.getString("mid");
        String uid = startLog.getString("uid");
        String os = startLog.getString("os");
        String appid = this.appId;
        String area = startLog.getString("area");
        String evid = eventOptionGroup.getRandomOpt().getValue();
        int pgid = new Random().nextInt(50) + 1;
        int npgid = new Random().nextInt(50) + 1;
        int itemid = new Random().nextInt(50);
        //  long ts= logDateUtil.getRandomDate().getTime();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "event");
        jsonObject.put("mid", mid);
        jsonObject.put("uid", uid);
        jsonObject.put("os", os);
        jsonObject.put("appid", appid);
        jsonObject.put("area", area);
        jsonObject.put("evid", evid);
        jsonObject.put("pgid", pgid);
        jsonObject.put("npgid", npgid);
        jsonObject.put("itemid", itemid);
        return jsonObject.toJSONString();
    }


    String initStartupLog() {
            /*`type` string   COMMENT '日志类型',
             `mid` string COMMENT '设备唯一标识',
      `uid` string COMMENT '用户标识',
      `os` string COMMENT '操作系统', ,
      `appId` string COMMENT '应用id', ,
     `vs` string COMMENT '版本号',
     `ts` bigint COMMENT '启动时间', ,
     `area` string COMMENT '城市' */


        String mid = "mid_" + RandomNum.getRandInt(1, 500);
        String uid = "" + RandomNum.getRandInt(1, 500);
        String os = osOptionGroup.getRandomOpt().getValue();
        String appid = this.appId;
        String area = areaOptionGroup.getRandomOpt().getValue();
        String vs = vsOptionGroup.getRandomOpt().getValue();
        //long ts= logDateUtil.getRandomDate().getTime();
        String ch = os.equals("ios") ? "appstore" : channelOptionGroup.getRandomOpt().getValue();


        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "startup");
        jsonObject.put("mid", mid);
        jsonObject.put("uid", uid);
        jsonObject.put("os", os);
        jsonObject.put("appid", appid);
        jsonObject.put("area", area);
        jsonObject.put("ch", ch);
        jsonObject.put("vs", vs);
        return jsonObject.toJSONString();
    }

    public static void genLog() {
        JsonMocker jsonMocker = new JsonMocker();
        jsonMocker.startupNum = 1000000;
        for (int i = 0; i < jsonMocker.startupNum; i++) {
            String startupLog = jsonMocker.initStartupLog();
            jsonMocker.sendLog(startupLog);
            while (!jsonMocker.isQuitGroup.getRandomOpt().getValue()) {
                String eventLog = jsonMocker.initEventLog(startupLog);
                jsonMocker.sendLog(eventLog);
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }


    public void sendLog(String log) {
        LogUploader.sendLogStream(log);
    }


    public static void main(String[] args) {
        genLog();
    }

}
