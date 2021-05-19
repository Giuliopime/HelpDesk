package dev.giuliopime.helpdesk.bot.internals.extentions

import dev.minn.jda.ktx.await
import kotlinx.coroutines.withTimeoutOrNull
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageReaction
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent

suspend inline fun Message.awaitReaction(
    crossinline filter: (MessageReactionAddEvent) -> Boolean = { true },
    timeout: Long = 60000
): MessageReaction? {
    return withTimeoutOrNull(timeout) {
        val event = jda.await<MessageReactionAddEvent> { filter(it) }
        return@withTimeoutOrNull event.reaction
    }
}

fun Message.getRole(): Role? {
    if (!isFromGuild)
        return null

    if (mentionedRoles.isNotEmpty())
        return mentionedRoles.first()

    try {
        val roleByID = guild.getRoleById(contentRaw)
        if (roleByID != null)
            return roleByID
    } catch (ignored: Exception) {}

    val roleByName = guild.getRolesByName(contentRaw, true)
    return if (roleByName.isEmpty()) null else roleByName.first()
}

