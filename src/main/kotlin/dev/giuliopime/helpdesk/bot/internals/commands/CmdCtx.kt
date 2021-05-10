package dev.giuliopime.helpdesk.bot.internals.commands

import dev.giuliopime.helpdesk.bot.internals.extentions.awaitReaction
import dev.giuliopime.helpdesk.bot.internals.frontend.Colors
import dev.giuliopime.helpdesk.bot.internals.frontend.Embeds
import dev.giuliopime.helpdesk.bot.internals.frontend.Reactions
import dev.giuliopime.helpdesk.data.guild.GuildD
import dev.minn.jda.ktx.Message
import dev.minn.jda.ktx.await
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.requests.ErrorResponse

class CmdCtx(
    message: Message,
    var cmd: AbstractCmd,
    var args: MutableList<String>,
    val flags: MutableList<String>,

    var guildData: GuildD,

    var helpDeskIndex: Int = -1,
) {
    val prefix = guildData.prefix

    val guild = message.guild
    val guildID = guild.id
    // Asserted non null because the bot doesn't listen to DMs and has a check for Webhook messages
    val member = message.member!!
    val userID = member.id
    val channel = message.textChannel

    val color = guild.selfMember.color ?: Colors.primary

    private var lastBotMsg: Message? = null

    @Throws(ErrorResponseException::class)
    suspend fun respond(embed: MessageEmbed, text: String? = null): Message {
        val message = Message(text, embed)

        return if (lastBotMsg == null) {
            lastBotMsg = channel.sendMessage(message).await()
            lastBotMsg!!
        } else {
            try {
                lastBotMsg!!.clearReactions().await()
                lastBotMsg!!.editMessage(message).await()
            } catch (e: ErrorResponseException) {
                if (e.errorResponse == ErrorResponse.UNKNOWN_MESSAGE) {
                    lastBotMsg = null
                    respond(embed, text)
                } else throw e
            }
        }
    }


    @Throws(UnsupportedOperationException::class)
    suspend fun operationSuccessfulBackToParentCmd(
        info: String,
        backTo: String = "the Help Desk editor.",
        unicodeEmoji: String = Reactions.Unicode.edit,
        extendedEmoji: String = Reactions.Extended.edit
    ) {
        if (cmd.parentCmd == null)
            throw UnsupportedOperationException("This command has no parent command to get back to")

        val embed = Embeds.operationSuccessfulBackToParentCmd(info, backTo, extendedEmoji)

        val sentMessage = if (lastBotMsg == null) {
            lastBotMsg = channel.sendMessage(embed).await()
            lastBotMsg!!
        } else {
            lastBotMsg!!.clearReactions().await()
            lastBotMsg!!.editMessage(embed).await()
        }

        sentMessage.addReaction(unicodeEmoji).queue()

        GlobalScope.async {
            val reaction = sentMessage.awaitReaction({
                it.messageId == sentMessage.id
                        && it.userId == userID
                        && it.reactionEmote.asReactionCode == unicodeEmoji
            })

            if (reaction == null) {
                sentMessage.clearReactions().queue()
                sentMessage.editMessage(Embeds.operationSuccessful(info)).queue()
            } else {
                cmd = CommandsHandler.getCommand(cmd.parentCmd!!.getDefaultPath())
                CommandsHandler.runCmd(this@CmdCtx)
            }
        }
    }
}
