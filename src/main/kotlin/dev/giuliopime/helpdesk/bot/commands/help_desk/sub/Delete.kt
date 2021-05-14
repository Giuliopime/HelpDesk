package dev.giuliopime.helpdesk.bot.commands.help_desk.sub

import dev.giuliopime.helpdesk.bot.commands.help_desk.HelpDesk
import dev.giuliopime.helpdesk.bot.internals.commands.AbstractCmd
import dev.giuliopime.helpdesk.bot.internals.commands.CmdCtx
import dev.giuliopime.helpdesk.bot.internals.commands.enums.CmdCategory
import dev.giuliopime.helpdesk.bot.internals.commands.enums.CmdUserPerms
import dev.giuliopime.helpdesk.bot.internals.frontend.Embeds
import dev.giuliopime.helpdesk.cache.handlers.GuildsHandler

class Delete: AbstractCmd(HelpDesk()) {
    init {
        name = "delete"
        aliases = listOf("del", "remove")
        description = "Allows you to delete an Help Desk."
        flags = listOf(Pair("-i", "The index of the Help Desk to delete"))
        usage = "(flags)"
        exampleUsages = listOf("", "-1")
        category = CmdCategory.HELP_DESK
        cooldown = 3000
        uniqueUsage = true
        requiresHelpDeskIndex = true
        userPerms = CmdUserPerms.MANAGE_CHANNELS
    }

    override suspend fun run(ctx: CmdCtx) {
        ctx.guildData.helpDesks.removeAt(ctx.helpDeskIndex)
        GuildsHandler.update(ctx.guildData)

        ctx.respond(Embeds.operationSuccessful("Help Desk deleted!\nYou can manually delete the Help Desk message / channel if you want."))
    }
}
