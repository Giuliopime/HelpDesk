package dev.giuliopime.helpdesk.utils

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.send.WebhookEmbed
import club.minnced.discord.webhook.send.WebhookEmbedBuilder
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import dev.giuliopime.helpdesk.bot.HelpDesk
import dev.giuliopime.helpdesk.bot.internals.Settings
import dev.giuliopime.helpdesk.bot.internals.frontend.Colors
import mu.KotlinLogging
import java.time.Instant

private val logger = KotlinLogging.logger {  }

object WebhooksService {
    private val errorsWHClient = WebhookClient.withUrl(Settings.Discord.errorWebhookUrl)
    private val errorsWHBuilder = WebhookMessageBuilder()
        .setUsername("Help Desk error logs")
        .setAvatarUrl(HelpDesk.shardsManager.shards.first().selfUser.effectiveAvatarUrl)
    private val errorEmbeds = mutableListOf<WebhookEmbed>()

    fun sendErrorWebhook(exception: Throwable) {
        val embed = WebhookEmbedBuilder()
            .setColor(Colors.red.rgb)
            .setTitle(WebhookEmbed.EmbedTitle(exception.message?.take(256) ?: "No exception message", null))
            .setDescription(exception.stackTraceToString().take(1500))
            .setTimestamp(Instant.now())
            .build()

        handleErrorEmbed(embed)
    }


    private fun handleErrorEmbed(embed: WebhookEmbed) {
        errorEmbeds.add(embed)
        if (errorEmbeds.size >= 3) {
            errorsWHBuilder.addEmbeds(errorEmbeds)
            errorEmbeds.clear()

            val message = errorsWHBuilder.build()

            errorsWHClient.send(message)
            errorsWHBuilder.resetEmbeds()
        }
    }

    fun shutdown() {
        errorsWHClient.close()
        logger.info("Webhooks service shutdown!")
    }
}
