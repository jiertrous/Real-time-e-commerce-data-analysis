package com.electricity.areatop.app

import java.util.UUID

import com.alibaba.fastjson.JSON
import net.sf.json.JSONObject
import com.electricity.areatop.conf.ConfigurationManager
import com.electricity.areatop.constant.Constants
import com.electricity.areatop.model.{OrderCountInfo, OrderSaleInfo, OrderTest}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

import scala.collection.mutable.ListBuffer




object AreaTop {
  def main(args: Array[String]): Unit = {
    // 获取筛选条件,获取json串
    val jsonStr = ConfigurationManager.config.getString(Constants.TASK_PARAMS)
    // 获取筛选条件对应的JsonObject
    val taskParam = JSONObject.fromObject(jsonStr)

    // 创建全局唯一的主键
    val taskUUID = UUID.randomUUID().toString

    // 创建sparkConf
    val sparkConf = new SparkConf().setAppName("session").setMaster("local[*]")

    // 创建sparkSession（包含sparkContext）
    val sparkSession = SparkSession.builder().config(sparkConf).enableHiveSupport().getOrCreate()


    // {name:"北京",value:"三星"}
    // RDD[(province_id,sku_tm_name)]
    val area2ProRDD = getAreaIdRDD(sparkSession)
//        area2ProRDD.foreach(println(_))

    // {name:"北京",value:200}
    //  RDD[(province_id,city_count)]
    val area2cityCountRDD = getAreaCountRDD(sparkSession)
    println(area2cityCountRDD.getClass.getSimpleName)

  }


  // {name:北京,value:"三星"},
  def getAreaIdRDD(sparkSession: SparkSession)= {
    val sql = "select " +
      " (case province_id " +
      "when 1 then '北京'  " +
      "when 2 then '上海'  " +
      "when 3 then '广东'  " +
      "when 4 then '江苏'  " +
      "when 5 then '河北'  " +
      "when 6 then '青海'  " +
      "when 7 then '西藏'  " +
      "when 8 then '黑龙江'" +
      "when 9 then '四川'  end)" +
      "as province_id," +
      "sku_tm_name " +
      "from  electricity_sale_info a,electricity_sale_detail b " +
      "where a.id = b.order_id and dt = '2020-02-26' " +
      "group by province_id,sku_tm_name"
    import sparkSession.implicits._
    sparkSession.sql("use electricity")
     sparkSession.sql(sql).as[OrderSaleInfo].rdd.map {
      case orderid => (orderid.province_id, orderid.sku_tm_name)
    }
  }
  // {name:"北京",value:200},
  def getAreaCountRDD(sparkSession: SparkSession) = {
    val sql = "select " +
      " (case province_id " +
      "when 1 then '北京'  " +
      "when 2 then '上海'  " +
      "when 3 then '广东'  " +
      "when 4 then '江苏'  " +
      "when 5 then '河北'  " +
      "when 6 then '青海'  " +
      "when 7 then '西藏'  " +
      "when 8 then '黑龙江'" +
      "when 9 then '四川'  end)" +
      "as province_id," +
      "count(*) as city_count " +
      "from electricity_sale_info " +
      "group by province_id"
    import sparkSession.implicits._
    sparkSession.sql("use electricity")
    sparkSession.sql(sql).as[OrderCountInfo].rdd.map {
      case citycount => (citycount.province_id, citycount.city_count)
    }
  }
}