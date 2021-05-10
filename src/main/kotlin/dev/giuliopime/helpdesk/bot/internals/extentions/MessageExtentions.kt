package dev.giuliopime.helpdesk.bot.internals.extentions

import dev.minn.jda.ktx.await
import kotlinx.coroutines.withTimeoutOrNull
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageReaction
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
