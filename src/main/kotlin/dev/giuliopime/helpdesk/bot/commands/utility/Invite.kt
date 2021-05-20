package dev.giuliopime.helpdesk.bot.commands.utility

import dev.giuliopime.helpdesk.bot.internals.commands.AbstractCmd
import dev.giuliopime.helpdesk.bot.internals.commands.CmdCtx
import dev.giuliopime.helpdesk.bot.internals.commands.enums.CmdCategory
import dev.giuliopime.helpdesk.bot.internals.frontend.URLs
import dev.minn.jda.ktx.Embed

class Invite: AbstractCmd() {
    init {
        name = "invite"
        aliases = listOf("inv")
        description = "Gives you a link to invite Help Desk in your server."
        category = CmdCategory.UTILITY
    }

    override suspend fun run(ctx: CmdCtx) {
        ctx.respond(
            Embed {
                color = ctx.color.rgb
                title = "Invite Help Desk in your server!"
                url = URLs.invite
            }
        )
    }
}
