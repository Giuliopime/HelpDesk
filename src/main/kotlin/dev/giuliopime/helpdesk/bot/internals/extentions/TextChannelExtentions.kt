package dev.giuliopime.helpdesk.bot.internals.extentions

import dev.minn.jda.ktx.await
import kotlinx.coroutines.withTimeoutOrNull
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException

suspend inline fun TextChannel.awaitMessageOrNull(
    crossinline filter: (GuildMessageReceivedEvent) -> Boolean = { true },
    autoDelete: Boolean = true,
    timeout: Long = 60000
): Message? {
    return withTimeoutOrNull(timeout) {
        val event = jda.await<GuildMessageReceivedEvent> {
            filter(it) && it.channel.id == id
        }

        if (autoDelete) {
            try {
                event.message.delete().queue()
            }
            catch (ignored: ErrorResponseException) {}
            catch (ignored: InsufficientPermissionException) {}
        }

        return@withTimeoutOrNull event.message
    }
}
