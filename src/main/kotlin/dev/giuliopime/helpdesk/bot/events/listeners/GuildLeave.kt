package dev.giuliopime.helpdesk.bot.events.listeners

import dev.giuliopime.helpdesk.data.guild.GuildForRemovalD
import dev.giuliopime.helpdesk.database.managers.GuildsForRemovalManager
import dev.giuliopime.helpdesk.timeseriesDB.controllers.GuildStatsController
import mu.KotlinLogging
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent

private val logger = KotlinLogging.logger {  }

fun onGuildLeave(event: GuildLeaveEvent) {
    val guild = event.guild

    GuildStatsController.writeGuildLeft(guild)
    logger.info("Left guild: ${guild.name} - members: ${guild.memberCount}")

    GuildsForRemovalManager.add(GuildForRemovalD(guild.id, System.currentTimeMillis()))
}
