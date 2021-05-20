package dev.giuliopime.helpdesk.bot.commands.help_desk.sub.edit_sub.questions

import dev.giuliopime.helpdesk.bot.commands.help_desk.sub.edit_sub.Questions
import dev.giuliopime.helpdesk.bot.internals.commands.AbstractCmd
import dev.giuliopime.helpdesk.bot.internals.commands.CmdCtx
import dev.giuliopime.helpdesk.bot.internals.commands.CommandsHandler
import dev.giuliopime.helpdesk.bot.internals.commands.enums.BotChannelPerms
import dev.giuliopime.helpdesk.bot.internals.extentions.awaitMessageOrNull
import dev.giuliopime.helpdesk.bot.internals.frontend.Colors
import dev.giuliopime.helpdesk.bot.internals.frontend.Embeds
import dev.giuliopime.helpdesk.bot.internals.frontend.Reactions
import dev.giuliopime.helpdesk.bot.internals.frontend.URLs
import dev.giuliopime.helpdesk.cache.handlers.GuildsHandler
import dev.minn.jda.ktx.Embed
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import net.dv8tion.jda.api.EmbedBuilder

class EditNotiChannel: AbstractCmd(Questions.HandleQuestion()) {
    init {
        name = "editNotiChannel"
        aliases = listOf("enc")
        description = "Can be used to edit the channel for the notification message of a question of the Help Desk"
        requiresArgs = true
        usage = "[question number]"
        exampleUsages = listOf("3", "20")
        cooldown = 2000
    }

    override suspend fun run(ctx: CmdCtx) {
        val index = ctx.args.first().toIntOrNull()?.minus(1)

        val questions = ctx.guildData.helpDesks[ctx.helpDeskIndex].questions
        val maxIndex = if (questions.size == 20) 20 else questions.size + 1

        if (index == null || index < 0 || index > maxIndex) {
            ctx.respond(Embeds.operationFailed("You didn't provide a valid number.", "Reuse the command and provide a number between 1 and $maxIndex."))
            return
        }

        ctx.respond(Embed {
            color = ctx.color.rgb
            title = "Channel selector"
            description = "**Mention the text channel where you want the Notification message to be sent.**\n" +
                    "*Send `delete` instead to unset the notification channel.*" +
                    "\n\n*Example: ${ctx.channel.asMention}*"
        })

        val channelMsg = ctx.channel.awaitMessageOrNull({
            it.author.id == ctx.userID && (it.message.contentRaw.toLowerCase() == "delete" || it.message.mentionedChannels.size > 0)
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

        val channel = if (channelMsg.contentRaw.toLowerCase() == "delete") null else channelMsg.mentionedChannels.first()

        if (channel != null && channel.guild.id != ctx.guildID) {
            ctx.respond(Embeds.operationFailed("The mentioned channel doesn't belong to this server.", "Reuse this command and provide a channel of this server."))
            return
        }

        if (channel != null && !ctx.guild.selfMember.hasPermission(channel, BotChannelPerms.MESSAGES.discordPermissions)) {
            val description = StringBuilder()
            description.append("**Help Desk is missing some of the following permissions in the mentioned channel:**")
            BotChannelPerms.MESSAGES.discordPermissions.forEach { description.append("\n• ${it.getName()}") }

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

        ctx.guildData = GuildsHandler.updateWithRoute(ctx.guildID, "helpDesks.${ctx.helpDeskIndex}.questions.$index.notificationChannel", channel?.id)

        ctx.cmd = CommandsHandler.getCommand(parentCmd!!.getDefaultPath())
        GlobalScope.async {
            CommandsHandler.runCmd(ctx)
        }
    }
}
