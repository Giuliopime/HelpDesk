package dev.giuliopime.helpdesk.bot.commands.help_desk.sub

import dev.giuliopime.helpdesk.bot.commands.help_desk.HelpDesk
import dev.giuliopime.helpdesk.bot.internals.commands.AbstractCmd
import dev.giuliopime.helpdesk.bot.internals.commands.CmdCtx
import dev.giuliopime.helpdesk.bot.internals.commands.CommandsHandler
import dev.giuliopime.helpdesk.bot.internals.commands.enums.BotChannelPerms
import dev.giuliopime.helpdesk.bot.internals.commands.enums.CmdCategory
import dev.giuliopime.helpdesk.bot.internals.commands.enums.CmdUserPerms
import dev.giuliopime.helpdesk.bot.internals.extentions.awaitSpecificMessage
import dev.giuliopime.helpdesk.bot.internals.extentions.takeFirstN
import dev.giuliopime.helpdesk.bot.internals.frontend.Embeds
import dev.giuliopime.helpdesk.bot.internals.frontend.Reactions
import dev.minn.jda.ktx.await
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.withTimeoutOrNull
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException

class Edit:AbstractCmd(HelpDesk()) {
    init {
        name = "edit"
        aliases = listOf("e")
        description = "Allows you to tweak your Help Desk to your liking and to configure useful features such as notification messages."
        flags = listOf(Pair("-i", "The index of the Help Desk to edit"))
        usage = "(flags)"
        exampleUsages = listOf("", "-2")
        category = CmdCategory.HELP_DESK
        userPerms = CmdUserPerms.MANAGE_CHANNELS
        botChannelPerms = BotChannelPerms.MESSAGES_CONTROL
        uniqueUsage = true
        requiresHelpDeskIndex = true
    }

    override suspend fun run(ctx: CmdCtx) {
        val baseURL = "https://helpdesk.giuliopime.dev"
        val hd = ctx.guildData.helpDesks[ctx.helpDeskIndex]

        val embed = EmbedBuilder()
            .setColor(ctx.color)
            .setTitle("${Reactions.Extended.edit} Help Desk Editor")

        embed.addField("\u200b\nInfo",
            "• `Channel` = <#${hd.channelID}>" +
                    "\n• `Message ID` = ${hd.messageID}" +
                    "\n• `Direct link` = [here](https://discord.com/channels/${ctx.guildID}/${hd.channelID}/${hd.messageID})" +
                    "\n\u200b",
            false
        )

        embed.addField("Questions (& Answers & Reactions)",
            "• [`Questions`]($baseURL) = Select to view more" +
                    "\n\u200B",
            false
        )

        embed.addField("Aesthetic",
            "• [`Help Desk message`]($baseURL) = Select to view more" +
                    "\n• [`Answers message`]($baseURL) = Select to view more" +
                    "\n\u200B",
            false
        )

        embed.addField("Notification",
            "• [`Channel`]($baseURL) = ${if(hd.notificationChannel != null) "<#${hd.notificationChannel}>" else "*not set*"}" +
                    "\n• [`Message`]($baseURL) = ${if(hd.notification != null) hd.notification.takeFirstN() else "*not set*"}" +
                    "\n\u200B",
            false
        )

        embed.addField("\u200b",
            "\n\n\n__**To select a setting send its name (or an abbreviation) in this chat.**__" +
                    "\nExamples:\n• `questions`\n• `answ`\n\n*Send `done` to exit this editor.*",
            false
        )

        ctx.respond(embed.build())


        val choices = listOf("questions", "help desk message", "answers message", "channel", "message", "done")

        val choice = ctx.channel.awaitSpecificMessage(ctx.userID, choices, 120000)

        when (choice) {
            null ->  {
                ctx.respond(Embeds.operationFailed("2 minutes time limit exceed.", "Next time send a value within 60 seconds."))
                return
            }
            "done" -> {
                ctx.respond(Embeds.operationSuccessful("Exited via `done`."))
                return
            }
        }

        val cmdName = when (choice) {
            "questions" -> "questions"
            "help desk message" -> "helpdeskmessage"
            "answers message" -> "answersmessage"
            "channel" -> "notichannel"
            "message" -> "notimessage"
            else -> "questions"
        }
        ctx.cmd = CommandsHandler.getCommand(getDefaultPath() + "/${cmdName}")
        GlobalScope.async {
            CommandsHandler.runCmd(ctx)
        }
    }
}
