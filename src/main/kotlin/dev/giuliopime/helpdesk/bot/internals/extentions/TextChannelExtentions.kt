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

suspend inline fun TextChannel.awaitSpecificMessage(
    userID: String,
    okStrings: List<String> = listOf(),
    timeout: Long = 60000,
    autoDelete: Boolean = true
): String? {
    return withTimeoutOrNull(timeout) {
        val event = jda.await<GuildMessageReceivedEvent> {
            it.author.id == userID
                    && it.channel.id == id
                    && okStrings.any { string -> string.toLowerCase().startsWith(it.message.contentRaw.toLowerCase()) }
        }

        if (autoDelete) {
            try { event.message.delete().queue() }
            catch (ignored: ErrorResponseException) { }
            catch (ignored: InsufficientPermissionException) { }
        }

        return@withTimeoutOrNull okStrings.find { it.toLowerCase().startsWith(event.message.contentRaw.toLowerCase()) }
            ?.toLowerCase()
    }
}


suspend inline fun TextChannel.awaitNumericMessage(
    userID: String,
    cancelString: String,
    upperLimit: Int,
    lowerLimit: Int = 1,
    timeout: Long = 60000,
    crossinline filter: (GuildMessageReceivedEvent) -> Boolean = { true },
    autoDelete: Boolean = true
): Int? {
    return withTimeoutOrNull(timeout) {
        val event = jda.await<GuildMessageReceivedEvent> {
            filter(it)
                    && it.author.id == userID
                    && it.channel.id == id
                    && (it.message.contentRaw.toLowerCase() == cancelString ||
                    (it.message.contentRaw.toIntOrNull() != null
                            && it.message.contentRaw.toInt() >= lowerLimit
                            && it.message.contentRaw.toInt() <= upperLimit))
        }

        if (autoDelete) {
            try { event.message.delete().queue() }
            catch (ignored: ErrorResponseException) { }
            catch (ignored: InsufficientPermissionException) { }
        }

        if (event.message.contentRaw.toLowerCase() == cancelString)
            return@withTimeoutOrNull -1

        return@withTimeoutOrNull event.message.contentRaw.toIntOrNull()
    }
}


