package dev.giuliopime.helpdesk.cache.handlers

import com.fasterxml.jackson.module.kotlin.readValue
import dev.giuliopime.helpdesk.cache.RedisClient
import dev.giuliopime.helpdesk.data.guild.GuildD
import dev.giuliopime.helpdesk.database.managers.GuildsManager
import dev.giuliopime.helpdesk.exceptions.DBOperationFailedException

object GuildsHandler {
    private val pool = RedisClient.redisPool
    private val jsonMapper = RedisClient.jsonMapper
    private const val hashName = "guilds"

    fun has(guildID: String): Boolean {
        pool.resource.use {
            val json = it.hget(hashName, guildID)

            return if (json == null) GuildsManager.exists(guildID) else true
        }
    }

    @Throws(DBOperationFailedException::class)
    fun getOrCreate(guildID: String): GuildD {
        pool.resource.use {
            val json = it.hget(hashName, guildID)

            return if (json == null) {
                try {
                    val guild = GuildsManager.getOrCreate(guildID)
                    cacheGuild(guild)
                    guild
                } catch (e: DBOperationFailedException) {
                    throw e
                }

            } else jsonMapper.readValue(json)
        }
    }

    @Throws(DBOperationFailedException::class)
    fun getOrNull(guildID: String): GuildD? {
        pool.resource.use {
            val json = it.hget(hashName, guildID)

            return if (json == null) {
                try {
                    val guild = GuildsManager.getOrNull(guildID)
                    if (guild != null)
                        cacheGuild(guild)
                    guild
                } catch (e: DBOperationFailedException) {
                    throw e
                }

            } else jsonMapper.readValue(json)
        }
    }

    @Throws(DBOperationFailedException::class)
    fun update(guildData: GuildD): GuildD {
        try {
            val guild = GuildsManager.update(guildData)
            cacheGuild(guild)
            return guild
        } catch (e: DBOperationFailedException) {
            throw e
        }
    }

    @Throws(DBOperationFailedException::class)
    fun updateWithRoute(guildID: String, route: String, value: Any?): GuildD {
        try {
            val guild = GuildsManager.updateWithRoute(guildID, route, value)
            cacheGuild(guild)
            return guild
        } catch (e: DBOperationFailedException) {
            throw e
        }
    }

    @Throws(DBOperationFailedException::class)
    fun delete(guildID: String) {
        try {
            GuildsManager.delete(guildID)
            uncacheGuild(guildID)
        } catch (e: DBOperationFailedException) {
            throw e
        }
    }

    @Throws(DBOperationFailedException::class)
    fun deleteAll(guildIDs: List<String>) {
        pool.resource.use {
            try {
                it.hdel(hashName, *guildIDs.toTypedArray())
                GuildsManager.deleteAll(guildIDs)
            } catch (e: DBOperationFailedException) {
                throw e
            }
        }
    }


    private fun cacheGuild(guildD: GuildD) {
        pool.resource.use {
            val json = jsonMapper.writeValueAsString(guildD)
            it.hset(hashName, guildD.guildID, json)
        }
    }

    private fun uncacheGuild(guildID: String) {
        pool.resource.use {
            it.hdel(hashName, guildID)
        }
    }
}
