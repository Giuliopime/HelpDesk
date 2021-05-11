package dev.giuliopime.helpdesk.bot.commands.guild

import dev.giuliopime.helpdesk.bot.internals.commands.AbstractCmd
import dev.giuliopime.helpdesk.bot.internals.commands.CmdCtx
import dev.giuliopime.helpdesk.bot.internals.commands.enums.CmdCategory
import dev.giuliopime.helpdesk.bot.internals.commands.enums.CmdUserPerms
import dev.giuliopime.helpdesk.bot.internals.frontend.Embeds
import dev.giuliopime.helpdesk.cache.handlers.GuildsHandler
import dev.minn.jda.ktx.Embed

class Prefix:AbstractCmd() {
    init {
        name = "prefix"
        description = "Shows or changes Help Desk's prefix."
        category = CmdCategory.GUILD
        cooldown = 3000
        uniqueUsage = true
        usage = "(new prefix)"
        exampleUsages = listOf("", "!", ".")
        userPerms = CmdUserPerms.MANAGE_CHANNELS
    }

    override suspend fun run(ctx: CmdCtx) {
        if (ctx.args.size > 0) {
            val prefix = ctx.args.joinToString(" ")
            if (prefix.length > 20) {
                ctx.respond(Embeds.operationFailed("Prefix is longer than 20 characters.", "Provide a prefix with less than 20 characters."))
                return
            }

            ctx.guildData = GuildsHandler.updateWithRoute(ctx.guildID, "prefix", prefix)

            ctx.respond(Embeds.operationSuccessful("Prefix changed to: `${prefix}`"))
        } else {
            ctx.respond(Embed {
                color = ctx.color.rgb
                description = "**Prefix for this server is `${ctx.prefix}`**\n\n*You can change it with `${ctx.prefix}prefix new_prefix`.*"
            })
        }
    }
}
