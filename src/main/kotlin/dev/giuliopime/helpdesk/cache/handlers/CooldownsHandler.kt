package dev.giuliopime.helpdesk.cache.handlers

import dev.giuliopime.helpdesk.bot.internals.commands.AbstractCmd
import dev.giuliopime.helpdesk.cache.RedisClient

object CooldownsHandler {
    private val pool = RedisClient.redisPool

    fun isUserOnGlobalCd(userID: String): Boolean {
        pool.resource.use {
            val hashName = "global_cooldowns"
            val currentTime = System.currentTimeMillis()

            val timestampString: String? = it.hget(hashName, userID)
            if (timestampString == null) {
                it.hset(hashName, userID, currentTime.toString())
            } else {
                try {
                    val timestamp = timestampString.toLong()
                    if (currentTime - timestamp < 200)
                        return true

                    it.hset(hashName, userID, currentTime.toString())
                } catch (e: NumberFormatException) {
                    it.hdel(hashName, userID)
                    return false
                }
            }

            return false
        }
    }


    fun getUserCmdCooldown(cmd: AbstractCmd, userID: String): Long {
        pool.resource.use {
            val hashName = "command_cooldowns"
            val field = "${userID}_${cmd.getDefaultPath()}"
            val currentTime = System.currentTimeMillis()

            val timestampString: String? = it.hget(hashName, "${userID}_${cmd.getDefaultPath()}")
            if (timestampString == null) {
                it.hset(hashName, field, currentTime.toString())
                return 0
            }

            return try {
                val timestamp = timestampString.toLong()
                if (currentTime - timestamp < cmd.cooldown) {
                    currentTime - timestamp
                } else {
                    it.hset(hashName, field, currentTime.toString())
                    0
                }
            } catch (e: NumberFormatException) {
                it.hdel(hashName, field)
                0
            }
        }
    }
}
