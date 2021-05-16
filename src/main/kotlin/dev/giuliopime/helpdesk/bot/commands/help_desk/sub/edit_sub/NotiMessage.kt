package dev.giuliopime.helpdesk.bot.commands.help_desk.sub.edit_sub

import dev.giuliopime.helpdesk.bot.commands.help_desk.sub.Edit
import dev.giuliopime.helpdesk.bot.internals.commands.AbstractCmd
import dev.giuliopime.helpdesk.bot.internals.commands.CmdCtx
import dev.giuliopime.helpdesk.bot.internals.commands.CommandsHandler
import dev.giuliopime.helpdesk.bot.internals.extentions.awaitMessageOrNull
import dev.giuliopime.helpdesk.bot.internals.frontend.Embeds
import dev.giuliopime.helpdesk.cache.handlers.GuildsHandler
import dev.minn.jda.ktx.Embed
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class NotiMessage: AbstractCmd(Edit()) {
    init {
        name = "notiMessage"
        aliases = listOf("nm", "notificationMessage")
        description = "Lets you choose the message for the notification of the Special Question."
    }

    override suspend fun run(ctx: CmdCtx) {
        ctx.respond(Embed {
            color = ctx.color.rgb
            description = "**Send the message for the Notification of the Special question of the Help Desk.**\n\nYou can use a bunch of variables in it such as:\n• `{mention}` will get replaced with the mention of the user who used the special question\n• `{name}` same as {mention} but for the user name instead\n\n(*You can use a maximum of 1000 characters*)"
        })

        val msg = ctx.channel.awaitMessageOrNull({
            it.author.id == ctx.userID && it.message.contentRaw.length <= 1000
        })

        if (msg == null) {
            ctx.respond(
                Embeds.operationFailed(
                    "60 seconds time limit exceeded.",
                    "Next time send a valid message within 60 seconds."
                )
            )
            return
        }

        ctx.guildData = GuildsHandler.updateWithRoute(ctx.guildID, "helpDesks.${ctx.helpDeskIndex}.notification", msg.contentRaw)

        ctx.cmd = CommandsHandler.getCommand(parentCmd!!.getDefaultPath())
        GlobalScope.async {
            CommandsHandler.runCmd(ctx)
        }
    }
}
