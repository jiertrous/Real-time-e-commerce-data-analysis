package com.demo.controller;

import com.demo.utils.GetDate;
import com.demo.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Controller
@RequestMapping(value = "/")
@PropertySource({"classpath:config/config.properties"})
public class IndexController {

    @Value("${my.totalUrl}")
    private String totalUrl;

    @Value("${my.hourUrl}")
    private String hourUrl;

    @Value("${my.esDataUrl}")
    private String esDataUrl;

    @Value("${my.sexUrl}")
    private String sexUrl;

    @Value("${my.cityUrl}")
    private String cityUrl;

    //测试
    @RequestMapping(value = "index1", method = RequestMethod.GET)
    public String index1() {
        return "index1";
    }

    //页面显示
    @RequestMapping(value = "index", method = RequestMethod.GET)
    public String index() {
        return "index";
    }

    @RequestMapping(value = "table", method = RequestMethod.GET)
    public String table() {
        return "table";
    }

    @RequestMapping(value = "map", method = RequestMethod.GET)
    public String map() {
        return "map";
    }


    @RequestMapping(value = "getTotal", method = RequestMethod.GET)
    @ResponseBody
    public String getTotal() {
        String sysDate = GetDate.getSysDate();
        return HttpClientUtil.doGet(totalUrl + "?date=" + sysDate);
    }

    //获取统计数据
    @RequestMapping(value = "getAnalysisData", method = RequestMethod.GET)
    @ResponseBody
    public String getList(String tag) throws IOException {
        String sysDate = GetDate.getSysDate();
        return HttpClientUtil.doGet(hourUrl + "?id=" + tag + "&&date=" + sysDate);
    }

    @RequestMapping(value = "getData", method = RequestMethod.GET)
    @ResponseBody
    public String getData(HttpServletRequest req) {

        String level = req.getParameter("level");
        String draw = req.getParameter("draw");
        String start = req.getParameter("start");
        String length = req.getParameter("length");

        String time = req.getParameter("time");
        String text = req.getParameter("text");
        int d = Integer.parseInt(draw);
        int s = Integer.parseInt(start) + 1;
        int l = Integer.parseInt(level);
        int size = Integer.parseInt(length);
        String sysDate = GetDate.getSysDate();
        String url = esDataUrl + "?startpage=" + s + "&&size=" + size;
        if (time != null && !"".equals(time)) {
            url = url + "&&date=" + time;
        } else {
            url = url + "&&date=" + "2020-02-03";
        }
        if (text != null && !"".equals(text)) {
            url = url + "&&keyword=" + text;
        } else {
            url = url + "&&keyword=" + "";
        }

        /*//获取前台额外传递过来的查询条件
        String extra_search = req.getParameter("extra_search");*/
        String json = HttpClientUtil.doGet(url);
        return "{'draw':" + draw + ",'data':" + json + "}";
    }

    @RequestMapping(value = "getSexData", method = RequestMethod.GET)
    @ResponseBody
    public String getSexData(String time, Integer level, String text) {

        return "{'stat':[{'group':[{'name':'20岁以下','value':300},{'name':'20-30岁','value':200},{'name':'30岁以上','value':100}]},{'group':[{'name':'男','value':200},{'name':'女','value':200}]}]}";
    }

    //获取地图数据
    @RequestMapping(value = "getCityName", method = RequestMethod.GET)
    @ResponseBody
    public String getChinaOrderData() {

        return HttpClientUtil.doGet(cityUrl);

    }
}
