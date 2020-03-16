package com.electricity.realtime.app

import redis.clients.jedis.Jedis

object testRedis {
  def main(args: Array[String]): Unit = {
    val jedis1 = new Jedis("master",6379)
    val ping = jedis1.ping()
    val key = "dau:"
    val value = "add"
    jedis1.sadd(key,value)
    println(ping)
  }
}
