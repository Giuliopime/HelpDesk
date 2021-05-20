package dev.giuliopime.helpdesk.bot.events.listeners

import dev.giuliopime.helpdesk.bot.internals.commands.enums.BotChannelPerms
import dev.giuliopime.helpdesk.bot.internals.frontend.Colors
import dev.giuliopime.helpdesk.bot.internals.frontend.Embeds
import dev.giuliopime.helpdesk.bot.internals.frontend.Reactions
import dev.giuliopime.helpdesk.bot.internals.frontend.URLs
import dev.giuliopime.helpdesk.cache.handlers.CooldownsHandler
import dev.giuliopime.helpdesk.cache.handlers.GuildsHandler
import dev.giuliopime.helpdesk.timeseriesDB.controllers.GuildStatsController
import dev.minn.jda.ktx.await
import kotlinx.coroutines.delay
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.exceptions.HierarchyException
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import java.time.Instant

suspend fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) {
    if (event.user.isBot || CooldownsHandler.isUserOnGlobalCd(event.userId))
        return

    val guildData = GuildsHandler.getOrNull(event.guild.id) ?: return
    val helpDesk = guildData.helpDesks.firstOrNull { it.messageID == event.messageId } ?: return

    if (!event.guild.selfMember.hasPermission(event.channel, BotChannelPerms.MESSAGES_CONTROL.discordPermissions)) {
        if (event.guild.selfMember.hasPermission(event.channel, BotChannelPerms.MESSAGES.discordPermissions))
            event.channel.sendMessage(Embeds.missingBotChannelPerms(BotChannelPerms.MESSAGES_CONTROL)).queue()
        return
    }

    event.reaction.removeReaction(event.user).queue()
    // Question detection
    val reaction = if (event.reactionEmote.isEmote) event.reactionEmote.emote.id else event.reactionEmote.name
    val question = helpDesk.questions.firstOrNull { it.reaction == reaction } ?: return

    GuildStatsController.writeQuestion(event.userId, event.guild.id, helpDesk.messageID)

    // Role handling
    val role = if (question.roleID != null) event.guild.getRoleById(question.roleID) else null
    if (role != null) {
        val failed = try {
            event.guild.addRoleToMember(event.member, role).await()
            false
        } catch (e: ErrorResponseException) {
            true
        } catch (e: InsufficientPermissionException) {
            true
        } catch (e: HierarchyException) {
            true
        }

        if (failed)
            event.channel.sendMessage(
                "I'm unable to assign the ${role.asMention} role. If you are a moderator of the server make sure I have `Manage_Roles` permissions and that the role I have to assign is under the Help Desk role in the server role hierarchy." +
                        "\nLearn more with [this article](https://support.discord.com/hc/en-us/articles/214836687-Role-Management-101)." +
                        "\n\nIf you are not a moderator of the server report this error to one of them."
            ).queue()
    }

    // Answer handling
    val answer = question.answer
        ?: if (question.roleID != null) "I assigned you the <@&${question.roleID}> role in ${event.guild.name}." else null

    if (answer != null) {
        val hdEmbed = helpDesk.answerEmbedProperties

        val embed = EmbedBuilder()
            .setColor(event.guild.selfMember.color ?: Colors.primary)

        try {
            embed.setAuthor(hdEmbed.author, hdEmbed.authorURL, hdEmbed.authorIcon)
            embed.setTitle(hdEmbed.title, hdEmbed.titleURL)
            embed.setDescription(answer)
            embed.setThumbnail(hdEmbed.thumbnail)
            embed.setImage(hdEmbed.image)
            embed.setFooter(hdEmbed.footer, hdEmbed.footerIcon)
            if(hdEmbed.timestamp != null) {
                try {
                    embed.setTimestamp(Instant.ofEpochMilli(hdEmbed.timestamp.toLong()))
                } catch (e: Exception) {
                    event.channel.sendMessage(Embeds.operationFailed(
                        "An invalid timestamp has been provided for the answer of this question.",
                        "**A moderator** of the server can use `${guildData.prefix}hd edit answersMessage` to set the `timestamp` to a correct timestamp, __you can get a timestamp of a date from [`this website`](https://www.unixtimestamp.com/) (always get the `Unix timestamp`)__."
                    )).queue()
                    return
                }
            }
        } catch (e: IllegalArgumentException) {
            event.channel.sendMessage(Embeds.operationFailed(
                "An invalid URL has been provided for the answer of this question.",
                "**A moderator** of the server can use `${guildData.prefix}hd edit answersMessage` to make sure that all the urls are correct internet links, __starting with `http` or `https`__."
            )).queue()
            return
        }

        val embedMsg = embed.build()

        if (!embedMsg.isSendable) {
            event.channel.sendMessage(Embeds.operationFailed(
                "The content for the answer is too long (over 6000 characters, limit set by Discord).",
                "**A moderator** of the server can use `${guildData.prefix}hd edit answersMessage` to reduce the amount of characters in the author, title, footer or just reduce the characters of the answer itself."
            )).queue()
            return
        }

        try {
            event.user.openPrivateChannel().await().sendMessage(embedMsg).await()
        } catch (e: ErrorResponseException) {
            val closedDMsMsg = event.channel.sendMessage("${event.member.asMention} your DMs are closed so I can't send you the answer of that question.\n*Open your DMs first, if you don't know how check out this article <https://support.discord.com/hc/en-us/articles/217916488-Blocking-Privacy-Settings->*").await()
            delay(1000L)
            try { closedDMsMsg.delete().queue() } catch (ignored: ErrorResponseException) {}
            return
        }
    }

    // Notification handling
    if (question.notificationMessage != null) {
        val notiChannel = if (question.notificationChannel != null) event.guild.getTextChannelById(question.notificationChannel) else null
        if (notiChannel != null) {
            val msgContent = question.notificationMessage
                .replace("{mention}", event.member.asMention, true)
                .replace("{name}", event.member.effectiveName, true)

            if (!event.guild.selfMember.hasPermission(notiChannel, BotChannelPerms.MESSAGES.discordPermissions)) {
                val description = StringBuilder()
                description.append("**I couldn't send the notification message in ${notiChannel.asMention} because I'm missing some of the following permissions in it:**")
                BotChannelPerms.MESSAGES.discordPermissions.forEach { description.append("\n• ${it.getName()}") }

                val embed = EmbedBuilder()
                    .setColor(Colors.red)
                    .setTitle("${Reactions.Extended.error} Missing permissions")
                    .setDescription(description.toString())
                    .addField("Quick fix", "Go in the `Channel Settings`, click `Permission` in the left side menu, add `Help Desk` and assign him the permissions listed above.", false)
                    .addField("Why are those permissions required?", "You can find out why Help Desk needs those permissions [here](${URLs.baseURL}).", false)
                    .build()

                event.channel.sendMessage(embed).queue()
                return
            }

            notiChannel.sendMessage(msgContent).queue()
        }
    }
}
