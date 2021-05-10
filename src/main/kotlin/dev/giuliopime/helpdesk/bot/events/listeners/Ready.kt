package dev.giuliopime.helpdesk.bot.events.listeners

import dev.giuliopime.helpdesk.bot.HelpDesk
import dev.giuliopime.helpdesk.utils.FixedRateTimers
import mu.KotlinLogging
import net.dv8tion.jda.api.events.ReadyEvent

private val logger = KotlinLogging.logger {}

fun onReady(event: ReadyEvent) {
    logger.info("Shard ${event.jda.shardInfo.shardId} successfully launched in ${System.currentTimeMillis() - HelpDesk.instance.launchTimestamp}ms")
    FixedRateTimers
}
