package dev.giuliopime.helpdesk.bot.commands.utility

import dev.giuliopime.helpdesk.bot.internals.commands.AbstractCmd
import dev.giuliopime.helpdesk.bot.internals.commands.CmdCtx
import dev.giuliopime.helpdesk.bot.internals.commands.enums.CmdCategory
import dev.giuliopime.helpdesk.bot.internals.frontend.Reactions
import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.await

class Ping: AbstractCmd() {
    init {
        name = "ping"
        aliases = listOf("latency")
        description = "Checks Help Desk's Discord latency"
        category = CmdCategory.UTILITY
        cooldown = 2000
    }

    override suspend fun run(ctx: CmdCtx) {
        val msgCreatedTimestamp = System.currentTimeMillis()

        ctx.respond(Embed {
            color = ctx.color.rgb
            description = "Pinging..."
        })

        val msgSendingPing = System.currentTimeMillis() - msgCreatedTimestamp
        val restPing = ctx.guild.jda.restPing.await()
        val gatewayPing = ctx.guild.jda.gatewayPing

        ctx.respond(Embed {
            color = ctx.color.rgb
            title = "Latency check"
            description = "${if (msgSendingPing < 300) Reactions.Extended.online else Reactions.Extended.idle} Message sending = *${msgSendingPing} ms*" +
                    "\n${if (restPing < 300) Reactions.Extended.online else Reactions.Extended.idle} Discord rest API = *${restPing} ms*" +
                    "\n${if (gatewayPing < 300) Reactions.Extended.online else Reactions.Extended.idle} Gateway / Websocket = *${gatewayPing} ms*"
        })
    }
}
