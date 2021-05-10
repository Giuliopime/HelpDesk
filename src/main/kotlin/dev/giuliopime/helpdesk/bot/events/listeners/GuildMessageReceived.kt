package dev.giuliopime.helpdesk.bot.events.listeners

import dev.giuliopime.helpdesk.bot.internals.commands.CmdCtx
import dev.giuliopime.helpdesk.bot.internals.commands.CommandsHandler
import dev.giuliopime.helpdesk.cache.handlers.CooldownsHandler
import dev.giuliopime.helpdesk.cache.handlers.GuildsHandler
import dev.giuliopime.helpdesk.data.guild.GuildD
import mu.KotlinLogging
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

private val logger = KotlinLogging.logger {  }

suspend fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
    val msg = event.message
    if (msg.author.isBot || msg.contentRaw == "" || msg.isWebhookMessage)
        return

    val member = event.member ?: return
    val guild = event.message.guild

    if (CooldownsHandler.isUserOnGlobalCd(member.id))
        return

    val guildData = GuildsHandler.getOrCreate(guild.id)

    val cmdSearchData = CommandsHandler.searchForCommand(guildData, msg) ?: return
    val cmd = cmdSearchData.cmd
    val args = cmdSearchData.args

    val ctx = CmdCtx(
        event.message,
        cmd,
        args,
        cmdSearchData.flags,
        guildData,
    )

    CommandsHandler.runCmd(ctx)
}
