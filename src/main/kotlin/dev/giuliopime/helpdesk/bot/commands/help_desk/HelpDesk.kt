package dev.giuliopime.helpdesk.bot.commands.help_desk

import dev.giuliopime.helpdesk.bot.internals.commands.AbstractCmd
import dev.giuliopime.helpdesk.bot.internals.commands.CmdCtx
import dev.giuliopime.helpdesk.bot.internals.commands.CommandsHandler
import dev.giuliopime.helpdesk.bot.internals.commands.enums.BotChannelPerms
import dev.giuliopime.helpdesk.bot.internals.commands.enums.CmdCategory
import dev.giuliopime.helpdesk.bot.internals.extentions.awaitReaction
import dev.giuliopime.helpdesk.bot.internals.frontend.Reactions
import dev.minn.jda.ktx.Embed
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class HelpDesk: AbstractCmd() {
    init {
        name = "helpdesk"
        aliases = listOf("hd", "helpdesks")
        description = "Gives you a control panel for all the help desks of your server. Via this panel you'll be able to quicky see, create, edit and delete help desks on the fly!"
        category = CmdCategory.HELP_DESK
        botChannelPerms = BotChannelPerms.MESSAGES_CONTROL
    }

    override suspend fun run(ctx: CmdCtx) {
        val prefix = ctx.prefix

        val embed = Embed {
            color = ctx.color.rgb
            title = "${Reactions.Extended.manage} Help Desks Panel"
            description = "View, create, edit and delete all your Help Desks on the fly via this panel!" +
                    "\n\n\n**${Reactions.Extended.list} List** all the existing Help Desks" +
                    "\n\n**${Reactions.Extended.create} Create** a new Help Desk" +
                    "\n\n**${Reactions.Extended.edit} Edit** the settings of an Help Desk" +
                    "\n\n**${Reactions.Extended.trash} Delete** an existing Help Desk" +
                    "\n\n\n• **You can use these commands via the matching reactions below this message**" +
                    "\n• You can also use these commands separately by placing `${prefix}helpdesk` (or an alias) before, for example: `${prefix}helpdesk list`"
        }


        val msg = ctx.respond(embed)
        val emojis = listOf(Reactions.Unicode.list, Reactions.Unicode.create, Reactions.Unicode.edit, Reactions.Unicode.trash)
        emojis.forEach { msg.addReaction(it).queue() }


        val reaction = msg.awaitReaction(
            {
                it.messageId == msg.id && it.userId == ctx.userID && emojis.contains(it.reactionEmote.asReactionCode)
            },
        )

        if (reaction != null) {
            val cmdToRunPath = "$name/" + when (reaction.reactionEmote.asReactionCode) {
                emojis[0] -> {
                    "list"
                }
                emojis[1] -> {
                    "create"
                }
                emojis[2] -> {
                    "edit"
                }
                else -> "delete"
            }

            ctx.cmd = CommandsHandler.getCommand(cmdToRunPath)
            ctx.helpDeskIndex = -1
            GlobalScope.async {
                CommandsHandler.runCmd(ctx)
            }
        } else {
            msg.delete().queue()
            ctx.message.delete().queue()
        }

    }
}
