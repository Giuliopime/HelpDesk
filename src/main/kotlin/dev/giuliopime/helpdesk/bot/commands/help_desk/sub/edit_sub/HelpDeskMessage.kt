package dev.giuliopime.helpdesk.bot.commands.help_desk.sub.edit_sub

import dev.giuliopime.helpdesk.bot.internals.commands.AbstractCmd
import dev.giuliopime.helpdesk.bot.internals.commands.CmdCtx
import dev.giuliopime.helpdesk.bot.internals.extentions.takeFirstN
import dev.giuliopime.helpdesk.bot.internals.frontend.Reactions
import dev.minn.jda.ktx.Embed

class HelpDeskMessage:AbstractCmd() {
    init {
        name = "helpdeskMessage"
        aliases = listOf("hdm", "hdMsg", "hdMessage")
        description = "Allows you to tweak the Help Desk message."
    }

    override suspend fun run(ctx: CmdCtx) {0
        val hdEmbed = ctx.guildData.helpDesks[ctx.helpDeskIndex].embedProperties

        val embed = Embed {
            color = ctx.color.rgb
            title = "${Reactions.Extended.edit} Help Desk Message Editor"
            description = "\n• `Author` = ${hdEmbed.author?.takeFirstN(20) ?: "Not set"}" +
                    "\n• `Author URL` = ${hdEmbed.authorURL?.takeFirstN(20) ?: "Not set"}" +
                    "\n• `Author icon` = ${hdEmbed.authorIcon?.takeFirstN(20) ?: "Not set"}" +
                    "\n\n• `Title` = ${hdEmbed.title?.takeFirstN(20) ?: "Not set"}" +
                    "\n• `Title URL` = ${hdEmbed.titleURL?.takeFirstN(20) ?: "Not set"}" +
                    "\n\n• `Description` = ${hdEmbed.description?.takeFirstN(20) ?: "Not set"}" +
                    "\n• `Thumbnail` = ${hdEmbed.thumbnail?.takeFirstN(20) ?: "Not set"}" +
                    "\n• `Image` = ${hdEmbed.image?.takeFirstN(20) ?: "Not set"}" +
                    "\n\n• `Footer` = ${hdEmbed.footer?.takeFirstN(20) ?: "Not set"}" +
                    "\n• `Footer Icon` = ${hdEmbed.footerIcon?.takeFirstN(20) ?: "Not set"}" +
                    "\n• `Timestamp` = ${hdEmbed.timestamp?.takeFirstN(20) ?: "Not set"}"
            field {
                name = "\u200b"
                value = "__**To change a value send its name (or an abbreviation) after this message.__**" +
                        "\nExamples:\n• `footer`\n• `thumb`"
                inline = false
            }
        }

        ctx.respond(embed)
    }
}
