package com.demo.controller;

import com.electricity.areatop.app.AreaTop;
import org.apache.spark.SparkConf;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.SparkSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import scala.Tuple2;
import scala.math.BigInt;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@RestController
public class DictController {

    @GetMapping("dict")
    public  String dict(HttpServletResponse response){
        response.addHeader("Last-Modified",new Date().toString());
        return "错误";
    }

}
