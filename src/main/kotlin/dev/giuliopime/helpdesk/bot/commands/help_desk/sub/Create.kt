package dev.giuliopime.helpdesk.bot.commands.help_desk.sub

import dev.giuliopime.helpdesk.bot.commands.help_desk.HelpDesk
import dev.giuliopime.helpdesk.bot.internals.commands.AbstractCmd
import dev.giuliopime.helpdesk.bot.internals.commands.CmdCtx
import dev.giuliopime.helpdesk.bot.internals.commands.CommandsHandler
import dev.giuliopime.helpdesk.bot.internals.commands.enums.BotChannelPerms
import dev.giuliopime.helpdesk.bot.internals.commands.enums.CmdCategory
import dev.giuliopime.helpdesk.bot.internals.commands.enums.CmdUserPerms
import dev.giuliopime.helpdesk.bot.internals.extentions.awaitMessageOrNull
import dev.giuliopime.helpdesk.bot.internals.frontend.Colors
import dev.giuliopime.helpdesk.bot.internals.frontend.Embeds
import dev.giuliopime.helpdesk.bot.internals.frontend.Reactions
import dev.giuliopime.helpdesk.cache.handlers.GuildsHandler
import dev.giuliopime.helpdesk.data.helpdesk.HelpDeskD
import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.await
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import net.dv8tion.jda.api.EmbedBuilder

class Create: AbstractCmd(HelpDesk()) {
    init {
        name = "create"
        aliases = listOf("new")
        description = "Allows you to create a new Help Desk for your server."
        category = CmdCategory.HELP_DESK
        cooldown = 5000
        uniqueUsage = true
        botChannelPerms = BotChannelPerms.MESSAGES_CONTROL
        userPerms = CmdUserPerms.MANAGE_CHANNELS
    }

    override suspend fun run(ctx: CmdCtx) {
        if (ctx.guildData.helpDesks.size >= 10) {
            ctx.respond(Embeds.operationCanceled("You already have 10 Help Desks in your server, you can't currently have more than that."))
            return
        }

        ctx.respond(Embed {
            color = ctx.color.rgb
            title = "Channel selector"
            description = "Mention the text channel where you want the Help Desk to be created.\n\n*Example: ${ctx.channel.asMention}*"
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
                .addField("Why are those permissions required?", "You can find out why Help Desk needs those permissions [here](link).", false)
                .build()
            ctx.respond(embed)
            return
        }

        val msg = channel.sendMessage(Embed {
            color = ctx.color.rgb
            description = "You can modify this Help Desk with `${ctx.prefix}helpdesk edit`"
        }).await()



        ctx.guildData.helpDesks.add(HelpDeskD(channelID = channel.id, messageID = msg.id))
        GuildsHandler.update(ctx.guildData)

        ctx.respond(Embeds.operationSuccessful("Help Desk created successfully in ${channel.asMention}.\n*Loading the Help Desk editor...*"))

        delay(2000L)

        ctx.cmd = CommandsHandler.getCommand("helpdesk/edit")
        ctx.helpDeskIndex = ctx.guildData.helpDesks.size - 1
        GlobalScope.async {
            CommandsHandler.runCmd(ctx)
        }
    }
}
