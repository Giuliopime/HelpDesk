package dev.giuliopime.helpdesk.bot.commands.help_desk.sub.edit_sub.questions

import dev.giuliopime.helpdesk.bot.commands.help_desk.sub.edit_sub.Questions
import dev.giuliopime.helpdesk.bot.internals.commands.AbstractCmd
import dev.giuliopime.helpdesk.bot.internals.commands.CmdCtx
import dev.giuliopime.helpdesk.bot.internals.commands.CommandsHandler
import dev.giuliopime.helpdesk.bot.internals.extentions.awaitMessageOrNull
import dev.giuliopime.helpdesk.bot.internals.frontend.Embeds
import dev.giuliopime.helpdesk.cache.handlers.GuildsHandler
import dev.minn.jda.ktx.Embed
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class EditQuestion: AbstractCmd(Questions.HandleQuestion()) {
    init {
        name = "editQuestion"
        aliases = listOf("eq")
        description = "Can be used to edit a question of the Help Desk"
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
            description = "**Send the question for the Help Desk after this message.**" +
                    "\n(*You can use maximum 1000 characters*)"
        })

        val question = ctx.channel.awaitMessageOrNull({
            it.author.id == ctx.userID
                    && it.message.contentRaw.length < 1000
        })

        if (question == null) {
            ctx.respond(Embeds.operationFailed("60 seconds time limit exceed.", "Next time send a value within 60 seconds."))
            return
        }

        ctx.guildData = GuildsHandler.updateWithRoute(ctx.guildID, "helpDesks.${ctx.helpDeskIndex}.questions.$index.question", question.contentRaw)

        ctx.cmd = CommandsHandler.getCommand(parentCmd!!.getDefaultPath())
        GlobalScope.async {
            CommandsHandler.runCmd(ctx)
        }
    }
}
