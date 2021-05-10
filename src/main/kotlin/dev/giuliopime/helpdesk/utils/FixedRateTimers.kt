package dev.giuliopime.helpdesk.utils

import dev.giuliopime.helpdesk.bot.HelpDesk
import dev.giuliopime.helpdesk.bot.internals.Settings
import dev.giuliopime.helpdesk.cache.handlers.GuildsHandler
import dev.giuliopime.helpdesk.data.topgg.TopggStatsD
import dev.giuliopime.helpdesk.database.managers.GuildsForRemovalManager
import dev.giuliopime.helpdesk.database.managers.GuildsManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import mu.KotlinLogging
import kotlin.concurrent.fixedRateTimer


private val logger = KotlinLogging.logger {  }

object FixedRateTimers {
    private val guildsForRemoval = fixedRateTimer("Guild for removal handler", false, 0L, 86400000) {
        val guildsForRemoval = GuildsForRemovalManager.getAll()
        val guildIDsToRemove = mutableListOf<String>()

        val currentTime = System.currentTimeMillis()

        guildsForRemoval.forEach {
            if (currentTime - it.timestampAdded >= 604800000)
                guildIDsToRemove.add(it.guildID)
        }

        if (guildIDsToRemove.isNotEmpty()) {
            GuildsForRemovalManager.deleteAll(guildIDsToRemove)
            GuildsHandler.deleteAll(guildIDsToRemove)
        }
    }

    private val topggStatsPoster = fixedRateTimer("Top.gg stats poster", false, 0L, 43200000) {
        if (!Settings.testing) {
            GlobalScope.async {
                ApisInteractor.postServerCount(
                    TopggStatsD(HelpDesk.shardsManager.guilds.size.toLong(), HelpDesk.shardsManager.shardsTotal)
                )
            }
        }
    }

    init {
        logger.info("Started fixed rate timers!")
    }

    fun shutdown() {
        guildsForRemoval.cancel()
        topggStatsPoster.cancel()
        logger.info("Fixed rate timers shutdown!")
    }
}
