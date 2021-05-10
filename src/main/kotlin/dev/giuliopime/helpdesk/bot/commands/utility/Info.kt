package dev.giuliopime.helpdesk.bot.commands.utility

import dev.giuliopime.helpdesk.bot.HelpDesk
import dev.giuliopime.helpdesk.bot.internals.Settings
import dev.giuliopime.helpdesk.bot.internals.commands.AbstractCmd
import dev.giuliopime.helpdesk.bot.internals.commands.CmdCtx
import dev.giuliopime.helpdesk.bot.internals.commands.enums.CmdCategory
import dev.minn.jda.ktx.Embed

class Info: AbstractCmd() {
    init {
        name = "info"
        aliases = listOf("stats")
        description = "Gives some general information about Help Desk."
        category = CmdCategory.UTILITY
    }

    override suspend fun run(ctx: CmdCtx) {
        ctx.respond(Embed {
            color = ctx.color.rgb
            thumbnail = ctx.guild.jda.selfUser.effectiveAvatarUrl
            title = "Help Desk Info & Stats"
            description = "__**Stats**__" +
                    "\n• Shards: `${HelpDesk.shardsManager.shards.size}`" +
                    "\n• Total servers: `${HelpDesk.shardsManager.guilds.size}`" +
                    "\n\n__**Development**__" +
                    "\n• Version: `1.0.0`" +
                    "\n• Uptime: `${Settings.getReadableUptime()}`" +
                    "\n• Library: [`JDA`](https://github.com/DV8FromTheWorld/JDA)" +
                    "\n• GitHub: [`Repository`](https://help-desk.giuliopime.dev/github)" +
                    "\n• Developer: `Giuliopime#4965`"
        })
    }
}
