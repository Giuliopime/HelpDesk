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
import dev.giuliopime.helpdesk.cache.handlers.GuildsHandler
import dev.minn.jda.ktx.Embed
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import net.dv8tion.jda.api.EmbedBuilder

class EditNotiMessage: AbstractCmd(Questions.HandleQuestion()) {
    init {
        name = "editNotiMessage"
        aliases = listOf("enm")
        description = "Can be used to edit the message for the notification of a question of the Help Desk"
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
            description = "**Send the message of the notification for the question of the Help Desk.**" +
                    "\n\nYou can use a bunch of variables in it such as:" +
                    "\n• `{mention}` will get replaced with the mention of the user who used the special question" +
                    "\n• `{name}` same as {mention} but for the user name instead" +
                    "\n(*You can use a maximum of 1000 characters*)" +
                    "\n\n*Send `delete` instead to unset the notification channel.*"
        })

        val msg = ctx.channel.awaitMessageOrNull({
            it.author.id == ctx.userID && it.message.contentRaw.length <= 1000
        })

        if (msg == null) {
            ctx.respond(
                Embeds.operationFailed(
                    "60 seconds time limit exceeded.",
                    "Next time send a valid message within 60 seconds."
                )
            )
            return
        }

        val msgContent = if (msg.contentRaw.toLowerCase() == "delete") null else msg.contentRaw

        ctx.guildData = GuildsHandler.updateWithRoute(ctx.guildID, "helpDesks.${ctx.helpDeskIndex}.questions.$index.notificationMessage", msgContent)

        ctx.cmd = CommandsHandler.getCommand(parentCmd!!.getDefaultPath())
        GlobalScope.async {
            CommandsHandler.runCmd(ctx)
        }
    }
}
