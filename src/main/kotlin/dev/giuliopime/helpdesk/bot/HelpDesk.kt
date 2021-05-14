package dev.giuliopime.helpdesk.bot

import dev.giuliopime.helpdesk.bot.events.EventsManager
import dev.giuliopime.helpdesk.bot.internals.Settings
import dev.giuliopime.helpdesk.cache.RedisClient
import dev.giuliopime.helpdesk.database.MongoDBClient
import dev.giuliopime.helpdesk.timeseriesDB.InfluxClient
import dev.giuliopime.helpdesk.utils.ApisConsumer
import dev.giuliopime.helpdesk.utils.FixedRateTimers
import dev.giuliopime.helpdesk.utils.WebhooksService
import dev.minn.jda.ktx.injectKTX
import mu.KotlinLogging
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.sharding.ShardManager
import net.dv8tion.jda.api.utils.cache.CacheFlag
import kotlin.concurrent.fixedRateTimer
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

class HelpDesk {
   val launchTimestamp: Long = System.currentTimeMillis()

    companion object {
        lateinit var instance: HelpDesk
        lateinit var shardsManager: ShardManager
        var shuttingDown = false
    }

    init {
        instance = this

        println("""
    __  __     __         ____            __  
   / / / /__  / /___     / __ \___  _____/ /__
  / /_/ / _ \/ / __ \   / / / / _ \/ ___/ //_/
 / __  /  __/ / /_/ /  / /_/ /  __(__  ) ,<   
/_/ /_/\___/_/ .___/  /_____/\___/____/_/|_|  
            /_/                               
            """".trimIndent())

        logger.info("Launching...")

        try {
            shardsManager = DefaultShardManagerBuilder
                .create(
                    GatewayIntent.DIRECT_MESSAGES,
                    GatewayIntent.GUILD_PRESENCES,
                    GatewayIntent.GUILD_MESSAGES,
                    GatewayIntent.GUILD_MESSAGE_REACTIONS,
                    GatewayIntent.GUILD_VOICE_STATES
                )
                .setToken(Settings.Discord.token)
                .setActivity(Activity.watching(Settings.Discord.status))
                .setStatus(OnlineStatus.ONLINE)
                .disableCache(CacheFlag.CLIENT_STATUS)
                .injectKTX()
                .build()

            EventsManager.manage(shardsManager)
        } catch (e: Exception) {
            logger.error("An error occurred while starting Help Desk!", e)
            WebhooksService.sendErrorWebhook(e)

            shutdown("ShardsBuilder exception was thrown")
        }
    }

    fun shutdown(reason: String) {
        logger.warn("Shutting down, reason: $reason")
        shuttingDown = true

        shardsManager.shutdown()
        FixedRateTimers.shutdown()
        RedisClient.shutdown()
        MongoDBClient.shutdown()
        InfluxClient.shutdown()
        WebhooksService.shutdown()
        ApisConsumer.shutdown()

        var counter = 0
        fixedRateTimer("Shutdown tryharder", false, 0L, 10000) {
            counter++

            if (shardsManager.statuses.all { it.value == JDA.Status.SHUTDOWN }) {
                logger.info("Successfully shutdown Help Desk")
                exitProcess(0)
            }

            if (counter >= 18) {
                logger.error("Couldn't shutdown the shards manager properly, it took over 3 minutes and it still hasn't finished")
                exitProcess(1)
            }
        }
    }
}
