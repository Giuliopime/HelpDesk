package dev.giuliopime.helpdesk.bot.commands.help_desk.sub.edit_sub

import dev.giuliopime.helpdesk.bot.commands.help_desk.sub.Edit
import dev.giuliopime.helpdesk.bot.internals.commands.AbstractCmd
import dev.giuliopime.helpdesk.bot.internals.commands.CmdCtx
import dev.giuliopime.helpdesk.bot.internals.frontend.Embeds
import dev.giuliopime.helpdesk.cache.handlers.GuildsHandler

class MoveQuestion: AbstractCmd(Edit()) {
    init {
        name = "moveQuestion"
        aliases = listOf("moveQuestions")
        description = "Allows you to move a question up and down the questions list."
        requiresArgs = true
        usage = "[current position] [new position]"
        exampleUsages = listOf("3 1", "4 7")
    }

    override suspend fun run(ctx: CmdCtx) {
        val currentPosition = ctx.args.removeFirst().toIntOrNull()?.minus(1)
        if (currentPosition == null) {
            ctx.respond(Embeds.operationFailed("You didn't provide a valid current position.", "Use `${ctx.prefix}help helpdesk edit moveQuestion` for info."))
            return
        }
        if (currentPosition > ctx.guildData.helpDesks[ctx.helpDeskIndex].questions.size || currentPosition < 0) {
            ctx.respond(Embeds.operationFailed("You didn't provide a valid current position.", "Use `${ctx.prefix}help helpdesk edit moveQuestion` for info."))
            return
        }

        val newPosition = ctx.args.firstOrNull()?.toIntOrNull()?.minus(1)

        if (newPosition == null) {
            ctx.respond(Embeds.operationFailed("You didn't provide a new position for the question.", "Use `${ctx.prefix}help helpdesk edit moveQuestion` for info."))
            return
        }
        if (newPosition > ctx.guildData.helpDesks[ctx.helpDeskIndex].questions.size || newPosition < 0 || newPosition == currentPosition) {
            ctx.respond(Embeds.operationFailed("You didn't provide a valid new position for the question.", "Use `${ctx.prefix}help helpdesk edit moveQuestion` for info."))
            return
        }

        val question = ctx.guildData.helpDesks[ctx.helpDeskIndex].questions[currentPosition]
        ctx.guildData.helpDesks[ctx.helpDeskIndex].questions.removeAt(currentPosition)
        ctx.guildData.helpDesks[ctx.helpDeskIndex].questions.add(newPosition, question)

        GuildsHandler.update(ctx.guildData)
        ctx.respond(Embeds.operationSuccessful("Question moved, use the `${ctx.prefix}helpdesk update` command to update the helpdesk."))
    }
}
