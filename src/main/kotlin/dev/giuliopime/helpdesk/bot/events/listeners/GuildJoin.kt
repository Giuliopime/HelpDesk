package dev.giuliopime.helpdesk.bot.events.listeners

import dev.giuliopime.helpdesk.bot.internals.commands.enums.BotChannelPerms
import dev.giuliopime.helpdesk.bot.internals.frontend.Colors
import dev.giuliopime.helpdesk.bot.internals.frontend.URLs
import dev.giuliopime.helpdesk.cache.handlers.GuildsHandler
import dev.giuliopime.helpdesk.data.guild.GuildD
import dev.giuliopime.helpdesk.database.managers.GuildsForRemovalManager
import dev.giuliopime.helpdesk.timeseriesDB.controllers.GuildStatsController
import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.await
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.exceptions.ErrorResponseException

private val logger = KotlinLogging.logger {  }

suspend fun onGuildJoin(event: GuildJoinEvent) {
    val guild = event.guild

    GuildStatsController.writeGuildJoin(guild)
    logger.info("Joined guild: ${guild.name} - members: ${guild.memberCount}")

    val guildDataExists = GuildsHandler.has(guild.id)
    val guildData = if (guildDataExists) GuildsHandler.getOrNull(guild.id) else null

    if (guildData != null) {
        try {
            GuildsForRemovalManager.deleteOne(guild.id)
        } catch (ignored: Exception) { }
    }

    var channel = guild.systemChannel ?: guild.defaultChannel
    if (channel == null || !guild.selfMember.hasPermission(channel, BotChannelPerms.MESSAGES.discordPermissions))
        channel = guild.textChannels.firstOrNull { guild.selfMember.hasPermission(it, BotChannelPerms.MESSAGES.discordPermissions)}

    if (channel != null) {
        channel.sendMessage(guildJoinEmbed(guild, guildData)).queue()
    } else {
        try {
            guild.retrieveOwner().await()
                .user.openPrivateChannel().await()
                .sendMessage(guildJoinEmbed(guild, guildData)).queue()
        } catch (ignored: ErrorResponseException) {}
    }
}

private fun guildJoinEmbed(guild: Guild, guildData: GuildD? = null): MessageEmbed {
    val prefix = guildData?.prefix ?: "hd?"

    return Embed {
        color = Colors.primary.rgb
        title = "Help Desk just landed in your server!"
        description = "Glad to have joined *${guild.name}*!"
        thumbnail = guild.selfMember.user.effectiveAvatarUrl
        field {
            name = "Prefix"
            value = "By default my prefix is `$prefix`" +
                    "\nYou can change it with either:" +
                    "\n• `${prefix}prefix new_prefix`" +
                    "\n• `@HelpDesk prefix new_prefix`" +
                    "\n\u200b"
            inline = false
        }
        field {
            name = "Get started quickly!"
            value = "Instantly get started creating Help Desks by running `${prefix}helpdesk` in your amazing server!\n\u200b"
            inline = false
        }
        field {
            name = "Take your time"
            value = "If you wanna fully understand how Help Desk works before starting setting it up, then take a look at the [`Documentation`](${URLs.baseURL}).\n\u200b"
            inline = false
        }
        field {
            name = "Command list"
            value = "You can get the Help Desk command list by running `${prefix}help`" +
                    "\n\n**[Invite](${URLs.invite})** | **[Documentation](${URLs.baseURL})** | **[Support](${URLs.support})** | **[GitHub](${URLs.github})**"
            inline = false
        }
        footer {
            name = "Enjoy ;)"
        }
    }
}
