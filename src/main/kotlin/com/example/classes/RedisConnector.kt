package com.example.classes

import redis.clients.jedis.Jedis

object RedisConnector {
    var db: Jedis? = Jedis()

    fun tryConnection(){
        if (db == null || !db!!.isConnected) {
            db = Jedis()
        }
    }
}