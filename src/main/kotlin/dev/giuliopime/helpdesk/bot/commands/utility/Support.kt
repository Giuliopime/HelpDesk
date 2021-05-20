package dev.giuliopime.helpdesk.bot.commands.utility

import dev.giuliopime.helpdesk.bot.internals.commands.AbstractCmd
import dev.giuliopime.helpdesk.bot.internals.commands.CmdCtx
import dev.giuliopime.helpdesk.bot.internals.commands.enums.CmdCategory
import dev.giuliopime.helpdesk.bot.internals.frontend.URLs

class Support: AbstractCmd() {
    init {
        name = "support"
        aliases = listOf("server")
        description = "Gives you a link to the Help Desk's support server."
        category = CmdCategory.UTILITY
    }

    override suspend fun run(ctx: CmdCtx) {
        ctx.channel.sendMessage(URLs.support).queue()
    }
}
