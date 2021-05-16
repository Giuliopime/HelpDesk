package dev.giuliopime.helpdesk.bot.events

import dev.giuliopime.helpdesk.bot.HelpDesk
import dev.giuliopime.helpdesk.bot.events.listeners.onGuildMessageReceived
import dev.giuliopime.helpdesk.bot.events.listeners.onReady
import dev.giuliopime.helpdesk.bot.internals.Settings
import dev.giuliopime.helpdesk.utils.WebhooksService
import dev.minn.jda.ktx.listener
import mu.KotlinLogging
import net.dv8tion.jda.api.events.*
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.sharding.ShardManager

private val logger = KotlinLogging.logger {}

object EventsManager {
    fun manage(shardManager: ShardManager) {
        shardManager.listener<GenericEvent> { event ->
            try {
                if (HelpDesk.shuttingDown)
                    return@listener
                when (event) {
                    is ReadyEvent -> onReady(event)

                    is GuildMessageReceivedEvent -> onGuildMessageReceived(event)

                    is ResumedEvent -> onResume(event)
                    is ReconnectedEvent -> onReconnect(event)
                    is DisconnectEvent -> onDisconnect(event)
                    is ExceptionEvent -> onException(event)
                }
            } catch (e: InsufficientPermissionException) {
                if (Settings.logPermissionExceptions)
                    logger.error("Permission error from the event manager", e)
            }
            catch (e: Exception) {
                logger.error("An uncaught exception occurred in the event manager", e)
                WebhooksService.sendErrorWebhook(e)
            }
        }
    }

    private fun onResume(event: ResumedEvent) {
        logger.info("Shard ${event.jda.shardInfo.shardId} has resumed")
    }

    private fun onReconnect(event: ReconnectedEvent) {
        logger.info("Shard ${event.jda.shardInfo.shardId} has reconnected")
    }

    private fun onDisconnect(event: DisconnectEvent) {
        if (event.isClosedByServer) {
            logger.info("Shard {} disconnected (closed by server). Code: {} {}",
                event.jda.shardInfo.shardId, event.serviceCloseFrame?.closeCode ?: -1, event.closeCode)
        } else {
            logger.info("Shard {} disconnected. Code: {} {}",
                event.jda.shardInfo.shardId, event.serviceCloseFrame?.closeCode
                    ?: -1, event.clientCloseFrame?.closeReason ?: "")
        }
    }

    private fun onException(event: ExceptionEvent) {
        if (!event.isLogged) {
            logger.error("Exception in Shard {}", event.jda.shardInfo.shardId, event.cause)
            WebhooksService.sendErrorWebhook(event.cause)
        }
    }
}

