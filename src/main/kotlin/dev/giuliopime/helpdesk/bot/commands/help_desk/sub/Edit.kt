package dev.giuliopime.helpdesk.bot.commands.help_desk.sub

import dev.giuliopime.helpdesk.bot.commands.help_desk.HelpDesk
import dev.giuliopime.helpdesk.bot.internals.commands.AbstractCmd
import dev.giuliopime.helpdesk.bot.internals.commands.CmdCtx
import dev.giuliopime.helpdesk.bot.internals.commands.CommandsHandler
import dev.giuliopime.helpdesk.bot.internals.commands.enums.BotChannelPerms
import dev.giuliopime.helpdesk.bot.internals.commands.enums.CmdCategory
import dev.giuliopime.helpdesk.bot.internals.commands.enums.CmdUserPerms
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

        embed.addField("Info",
            "• `Channel` = <#${hd.channelID}>" +
                    "\n• `Message ID` = ${hd.messageID}" +
                    "\n• `Direct link` = [here](https://discord.com/channels/${ctx.guildID}/${hd.channelID}/${hd.messageID})",
            false
        )

        embed.addField("Questions & Answers",
            "• [`Questions`]($baseURL) = Select to view more" +
                    "\n• [`Answers`]($baseURL) = Select to view more",
            false
        )

        embed.addField("Aesthetic",
            "• [`Help Desk message`]($baseURL) = Select to view more" +
                    "\n• [`Answers message`]($baseURL) = Select to view more" +
                    "\n• [`Reactions`]($baseURL) = Select to view more",
            false
        )

        embed.addField("Notification",
            "• [`Channel`]($baseURL) = ${if(hd.notificationChannel != null) "<#${hd.notificationChannel}>" else "*not set*"}" +
                    "\n• [`Message`]($baseURL) = ${if(hd.notification != null) hd.notification.takeFirstN() else "*not set*"}",
            false
        )

        embed.addField("\u200b",
            "\n\n\n__**To select a setting send its name (or an abbreviation) in this chat.**__" +
                    "\nExamples:\n• `answers`\n• ``\n\n*Send `done` to exit this editor.*",
            false
        )

        ctx.respond(embed.build())


        val choices = listOf("questions", "answers", "help desk message", "answers message", "reactions", "channel", "message", "done")

        val choice = withTimeoutOrNull(120000) {
            val event = ctx.channel.jda.await<GuildMessageReceivedEvent> {
                it.author.id == ctx.userID
                        && it.channel.id == ctx.channel.id
                        && choices.any { string -> string.toLowerCase().startsWith(it.message.contentRaw.toLowerCase()) }
            }

            try {
                event.message.delete().queue()
            }
            catch (ignored: ErrorResponseException) {}
            catch (ignored: InsufficientPermissionException) {}

            return@withTimeoutOrNull choices.find { it.toLowerCase().startsWith(event.message.contentRaw.toLowerCase()) }
                ?.toLowerCase()
        }

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
            else -> "questions"
        }
        ctx.cmd = CommandsHandler.getCommand(getDefaultPath() + "/${cmdName}")
        GlobalScope.async {
            CommandsHandler.runCmd(ctx)
        }
    }
}
