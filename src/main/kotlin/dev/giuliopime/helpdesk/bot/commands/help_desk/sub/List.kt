package dev.giuliopime.helpdesk.bot.commands.help_desk.sub

import dev.giuliopime.helpdesk.bot.commands.help_desk.HelpDesk
import dev.giuliopime.helpdesk.bot.internals.commands.AbstractCmd
import dev.giuliopime.helpdesk.bot.internals.commands.CmdCtx
import dev.giuliopime.helpdesk.bot.internals.commands.enums.CmdCategory
import dev.giuliopime.helpdesk.bot.internals.frontend.Reactions
import dev.minn.jda.ktx.Embed

class List: AbstractCmd(HelpDesk()) {
    init {
        name = "list"
        aliases = listOf("l", "ls")
        description = "Gives you the list of Help Desks of the server."
        category = CmdCategory.HELP_DESK
        cooldown = 2000
    }

    override suspend fun run(ctx: CmdCtx) {
        val descriptionBuilder = StringBuilder()
        descriptionBuilder.append("To view more information about an Help Desk use `${ctx.prefix}helpdesk edit`.")

        if (ctx.guildData.helpDesks.size == 0)
            descriptionBuilder.append("\n\n__This server doesn't have any Help Desk.__")

        for ((index, helpdesk) in ctx.guildData.helpDesks.withIndex())
            descriptionBuilder.append("\n\n`${index + 1}.` **<#${helpdesk.channelID}>** [message ID: `${helpdesk.messageID}`]")

        ctx.respond(Embed {
            color = ctx.color.rgb
            title = "${Reactions.Extended.list} Help Desks List"
            description = descriptionBuilder.toString()
        })
    }
}
