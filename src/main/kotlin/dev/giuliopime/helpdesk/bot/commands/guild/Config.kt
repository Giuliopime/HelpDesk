package dev.giuliopime.helpdesk.bot.commands.guild

import dev.giuliopime.helpdesk.bot.internals.commands.AbstractCmd
import dev.giuliopime.helpdesk.bot.internals.commands.CmdCtx
import dev.giuliopime.helpdesk.bot.internals.commands.enums.CmdCategory
import dev.minn.jda.ktx.Embed
import java.time.Instant

class Config: AbstractCmd() {
    init {
        name = "config"
        aliases = listOf("conf", "configs")
        description = "Show the current Help Desk configuration for the server."
        category = CmdCategory.GUILD
        cooldown = 2000
    }

    override suspend fun run(ctx: CmdCtx) {
        val baseURL = "https://helpdesk.giuliopime.dev"

        val embed = Embed {
            title = "Help Desk configuration for ${ctx.guild.name}"
            color = ctx.color.rgb
            description = "__**Settings**__" +
                    "\n• [Prefix]($baseURL) = `${ctx.prefix}`" +
                    "\n" +
                    "\n__**Help Desks**__\n${
                        if (ctx.guildData.helpDesks.size == 0)
                            "This server doesn't have any Help Desk, you can create one with `${ctx.prefix}helpdesk create` or via the Help Desk panel `${ctx.prefix}helpdesk`."
                        else
                            "Found ${ctx.guildData.helpDesks.size} Help Desk${if (ctx.guildData.helpDesks.size == 1) "" else "s"}.\nUse the Help Desk panel to manage them `${ctx.prefix}helpdesk`."
                    }"
            thumbnail = ctx.guild.iconUrl
            footer {
                name = "Use ${ctx.prefix}help for a list of commands"
                iconUrl = ctx.guild.jda.selfUser.effectiveAvatarUrl
            }
            timestamp = Instant.now()
        }

        ctx.respond(embed)
    }
}
