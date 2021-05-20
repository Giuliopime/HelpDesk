package dev.giuliopime.helpdesk.bot.commands.help_desk.sub

import dev.giuliopime.helpdesk.bot.commands.help_desk.HelpDesk
import dev.giuliopime.helpdesk.bot.internals.commands.AbstractCmd
import dev.giuliopime.helpdesk.bot.internals.commands.CmdCtx
import dev.giuliopime.helpdesk.bot.internals.commands.enums.BotChannelPerms
import dev.giuliopime.helpdesk.bot.internals.commands.enums.CmdCategory
import dev.giuliopime.helpdesk.bot.internals.commands.enums.CmdUserPerms
import dev.giuliopime.helpdesk.bot.internals.frontend.Colors
import dev.giuliopime.helpdesk.bot.internals.frontend.Embeds
import dev.giuliopime.helpdesk.bot.internals.frontend.Reactions
import dev.giuliopime.helpdesk.bot.internals.frontend.URLs
import dev.giuliopime.helpdesk.data.helpdesk.QuestionD
import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.await
import io.ktor.http.*
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageReaction
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import java.lang.NumberFormatException
import java.time.Instant

class Update:AbstractCmd(HelpDesk()) {
    init {
        name = "update"
        aliases = listOf("u")
        description = "Applies the settings you have set via the help desk edit panel to the actual Help Desk."
        flags = listOf(Pair("-i", "The index of the Help Desk to update"))
        usage = "(flags)"
        exampleUsages = listOf("", "-2")
        category = CmdCategory.HELP_DESK
        userPerms = CmdUserPerms.MANAGE_CHANNELS
        botChannelPerms = BotChannelPerms.MESSAGES_CONTROL
        uniqueUsage = true
        requiresHelpDeskIndex = true
    }

    override suspend fun run(ctx: CmdCtx) {
        val hd = ctx.guildData.helpDesks[ctx.helpDeskIndex]


        val channel = ctx.guild.getTextChannelById(hd.channelID)
        if (channel == null) {
            ctx.respond(Embeds.operationFailed(
                "The channel of the Help Desk (with ID `${hd.channelID}`) doesn't exist anymore.",
                "Use the `${ctx.prefix}hd resendMessage${if (ctx.helpDeskIndex > 0) " -${ctx.helpDeskIndex + 1}" else ""}` command to regenerate the Help Desk in another channel."
            ))
            return
        }

        if (!ctx.guild.selfMember.hasPermission(channel, BotChannelPerms.MANAGE_HELP_DESK.discordPermissions)) {
            val description = StringBuilder()
            description.append("**Help Desk is missing some of the following permissions in <#${channel.id}>:**")
            BotChannelPerms.MANAGE_HELP_DESK.discordPermissions.forEach { description.append("\n• ${it.getName()}") }

            val embed = EmbedBuilder()
                .setColor(Colors.red)
                .setTitle("${Reactions.Extended.error} Missing permissions")
                .setDescription(description.toString())
                .addField("Quick fix", "Go in the `Channel Settings`, click `Permission` in the left side menu, add `Help Desk` and assign him the permissions listed above.", false)
                .addField("Why are those permissions required?", "You can find out why Help Desk needs those permissions [here](${URLs.baseURL}).", false)
                .build()
            ctx.respond(embed)
            return
        }

        val msg = try {
            channel.retrieveMessageById(hd.messageID).await()
        } catch (e: ErrorResponseException) {
            ctx.respond(Embeds.operationFailed(
                "I couldn't find the Help Desk message in <#${channel.id}>.",
                "Use the `${ctx.prefix}hd resendMessage${if (ctx.helpDeskIndex > 0) " -${ctx.helpDeskIndex + 1}" else ""}` command to regenerate the Help Desk."
            ))
            return
        }


        val hdEmbed = hd.embedProperties

        val newEmbed = EmbedBuilder()
            .setColor(ctx.color)

        try {
            newEmbed.setAuthor(hdEmbed.author, hdEmbed.authorURL, hdEmbed.authorIcon)
            newEmbed.setDescription(hdEmbed.description)
            newEmbed.setThumbnail(hdEmbed.thumbnail)
            newEmbed.setImage(hdEmbed.image)
            newEmbed.setFooter(hdEmbed.footer, hdEmbed.footerIcon)
            if(hdEmbed.timestamp != null) {
                try {
                    newEmbed.setTimestamp(Instant.ofEpochMilli(hdEmbed.timestamp.toLong()))
                } catch (e: Exception) {
                    ctx.respond(Embeds.operationFailed(
                        "An invalid timestamp has been provided for the Help Desk message.",
                        "Use `${ctx.prefix}hd edit helpdeskmessage${if (ctx.helpDeskIndex > 0) " -${ctx.helpDeskIndex + 1}" else ""}` and set the `timestamp` to a correct timestamp, __you can get a timestamp of a date from [`this website`](https://www.unixtimestamp.com/) (always get the `Unix timestamp`)__."
                    ))
                    return
                }
            }
        } catch (e: IllegalArgumentException) {
            ctx.respond(Embeds.operationFailed(
                "An invalid URL has been provided for the Help Desk message.",
                "Use `${ctx.prefix}hd edit helpdeskmessage${if (ctx.helpDeskIndex > 0) " -${ctx.helpDeskIndex + 1}" else ""}` and make sure that all the urls are correct internet links, __starting with `http` or `https`__."
            ))
            return
        }

        val reactionsExtended = mutableListOf<String>()

        for ((i, q) in hd.questions.withIndex()) {
            val reaction = if (q.reaction == null) null
            else if (q.reaction.toLongOrNull() != null) {
                try {
                    ctx.guild.retrieveEmoteById(q.reaction).await().asMention
                } catch (e: ErrorResponseException) { null } catch (e: IllegalArgumentException) { null }
            }
            else q.reaction

            if (q.question == null || reaction == null) {
                ctx.respond(Embeds.operationFailed(
                    "A question of the Help Desk is missing either the:" +
                            "\n• `question` itself" +
                            "\n• `reaction` for the question",
                    "Use `${ctx.prefix}hd edit questions${if (ctx.helpDeskIndex > 0) " -${ctx.helpDeskIndex + 1}" else ""}` and make sure that all the questions have a valid value for both the `question` itself and its `reaction`\n\n**This issue was reported on the question number `${i+1}`**."
                ))
                return
            }

            reactionsExtended.add(reaction)
        }

        for ((i, q) in hd.questions.withIndex()) {
            newEmbed.addField("\u200b", "${reactionsExtended[i]} ${q.question}", false)
        }

        val newEmbedMsg = newEmbed.build()

        if (!newEmbedMsg.isSendable) {
            ctx.respond(Embeds.operationFailed(
                "The content for the Help Desk is too long (over 6000 characters, limit set by Discord).",
                "Edit the Help Desk and reduce the amount of characters in the author, title, description, footer or of the questions."
            ))
            return
        }

        ctx.respond(Embed {
            color = ctx.color.rgb
            description = "Updating..."
        })

        val editedMsg = msg.editMessage(newEmbed.build()).await()

        editedMsg.clearReactions().await()

        hd.questions.forEach {
            val emote = if (it.reaction?.toLongOrNull() != null) {
                ctx.guild.retrieveEmoteById(it.reaction).await()
            } else null

            if (emote != null)
                editedMsg.addReaction(emote).await()
            else
                editedMsg.addReaction(it.reaction!!).await()
        }

        ctx.respond(Embeds.operationSuccessful("[`Help Desk`](${URLs.msgLink(ctx.guildID, hd.channelID, hd.messageID)}) correctly updated!"))
    }
}
