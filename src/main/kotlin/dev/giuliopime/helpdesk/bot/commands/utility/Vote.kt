package dev.giuliopime.helpdesk.bot.commands.utility

import dev.giuliopime.helpdesk.bot.internals.commands.AbstractCmd
import dev.giuliopime.helpdesk.bot.internals.commands.CmdCtx
import dev.giuliopime.helpdesk.bot.internals.commands.enums.CmdCategory
import dev.minn.jda.ktx.Embed

class Vote: AbstractCmd() {
    init {
        name = "vote"
        description = "Gives you a link to vote for Help Desk."
        category = CmdCategory.UTILITY
    }

    override suspend fun run(ctx: CmdCtx) {
        ctx.respond(Embed {
            color = ctx.color.rgb
            title = "Vote for Help Desk"
            url = "https://help-desk.giuliopime.dev/vote"
        })
    }
}
