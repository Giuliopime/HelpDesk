package dev.giuliopime.helpdesk.cache

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.giuliopime.helpdesk.bot.internals.Settings
import mu.KotlinLogging
import redis.clients.jedis.JedisPool

private val logger = KotlinLogging.logger {}

object RedisClient {
    private val redisHost: String = Settings.getFromEnv("redis.host")
    private val redisPort: Int = Settings.getFromEnv("redis.port").toInt()

    val jsonMapper = jacksonObjectMapper()

    val redisPool = JedisPool(redisHost, redisPort)

    fun shutdown() {
        redisPool.close()
        logger.info("Redis client shutdown")
    }
}
