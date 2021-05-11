package dev.giuliopime.helpdesk.bot.commands.utility

import dev.giuliopime.helpdesk.bot.internals.commands.AbstractCmd
import dev.giuliopime.helpdesk.bot.internals.commands.CmdCtx
import dev.giuliopime.helpdesk.bot.internals.commands.enums.CmdCategory

class Support: AbstractCmd() {
    init {
        name = "support"
        aliases = listOf("server")
        description = "Gives you a link to the Help Desk's support server."
        category = CmdCategory.UTILITY
    }

    override suspend fun run(ctx: CmdCtx) {
        ctx.channel.sendMessage("https://helpdesk.giuliopime.dev/support").queue()
    }
}
