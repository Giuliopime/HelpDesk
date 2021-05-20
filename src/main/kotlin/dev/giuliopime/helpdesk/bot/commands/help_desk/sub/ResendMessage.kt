package dev.giuliopime.helpdesk.bot.commands.help_desk.sub

import dev.giuliopime.helpdesk.bot.commands.help_desk.HelpDesk
import dev.giuliopime.helpdesk.bot.internals.commands.AbstractCmd
import dev.giuliopime.helpdesk.bot.internals.commands.CmdCtx
import dev.giuliopime.helpdesk.bot.internals.commands.enums.BotChannelPerms
import dev.giuliopime.helpdesk.bot.internals.commands.enums.CmdCategory
import dev.giuliopime.helpdesk.bot.internals.commands.enums.CmdUserPerms
import dev.giuliopime.helpdesk.bot.internals.extentions.awaitMessageOrNull
import dev.giuliopime.helpdesk.bot.internals.frontend.Colors
import dev.giuliopime.helpdesk.bot.internals.frontend.Embeds
import dev.giuliopime.helpdesk.bot.internals.frontend.Reactions
import dev.giuliopime.helpdesk.bot.internals.frontend.URLs
import dev.giuliopime.helpdesk.cache.handlers.GuildsHandler
import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.EmbedBuilder

class ResendMessage: AbstractCmd(HelpDesk()) {
    init {
        name = "resendMessage"
        aliases = listOf("sendMessage")
        description = "Allows you to recreate the Help Desk in case it has been deleted."
        flags = listOf(Pair("-i", "The index of the Help Desk to edit"))
        usage = "(flags)"
        exampleUsages = listOf("", "-2")
        cooldown = 2000
        category = CmdCategory.HELP_DESK
        userPerms = CmdUserPerms.MANAGE_CHANNELS
        botChannelPerms = BotChannelPerms.MESSAGES_CONTROL
        uniqueUsage = true
        requiresHelpDeskIndex = true
    }

    override suspend fun run(ctx: CmdCtx) {
        ctx.respond(Embed {
            color = ctx.color.rgb
            title = "Channel selector"
            description = "Mention the text channel where you want the Help Desk to be sent.\n\n*Example: ${ctx.channel.asMention}*"
        })

        val channelMsg = ctx.channel.awaitMessageOrNull({
            it.author.id == ctx.userID && it.message.mentionedChannels.size > 0
        })

        if (channelMsg == null) {
            ctx.respond(
                Embeds.operationFailed(
                    "60 seconds time limit exceeded.",
                    "Next time send a valid channel mention within 60 seconds."
                )
            )
            return
        }

        val channel = channelMsg.mentionedChannels.first()

        if (channel.guild.id != ctx.guildID) {
            ctx.respond(Embeds.operationFailed("The mentioned channel doesn't belong to this server.", "Reuse this command and provide a channel of this server."))
            return
        }

        if (!ctx.guild.selfMember.hasPermission(channel, BotChannelPerms.MANAGE_HELP_DESK.discordPermissions)) {
            val description = StringBuilder()
            description.append("**Help Desk is missing some of the following permissions in the mentioned channel:**")
            BotChannelPerms.MANAGE_HELP_DESK.discordPermissions.forEach { description.append("\n• ${it.getName()}") }

            val embed = EmbedBuilder()
                .setColor(Colors.red)
                .setTitle("${Reactions.Extended.error} Missing permissions")
                .setDescription(description.toString())
                .addField("Quick fix", "Go in the `Channel Settings`, click `Permission` in the left side menu, add `Help Desk` and assign him the permissions listed above.", false)
                .addField("Why are those permissions required?", "You can find out why Help Desk needs those permissions [here](${URLs.baseURL}).", false)
                .build()
            ctx.respond(embed)
            return
        }

        val msg = channel.sendMessage(Embed {
            color = ctx.color.rgb
            description = "**This is the resent Help Desk**\n\n**Use `${ctx.prefix}helpdesk update` to synchronize the content with the settings (aka update the Help Desk).**"
        }).await()

        ctx.guildData.helpDesks[ctx.helpDeskIndex].messageID = msg.id
        ctx.guildData.helpDesks[ctx.helpDeskIndex].channelID = channel.id

        GuildsHandler.updateWithRoute(ctx.guildID, "helpDesks", ctx.guildData.helpDesks)

        ctx.respond(Embeds.operationSuccessful("*Help Desk resent in ${channel.asMention}.*\n\n**Use `${ctx.prefix}helpdesk update` to synchronize the content with the settings (aka update the Help Desk).**"))
    }
}
