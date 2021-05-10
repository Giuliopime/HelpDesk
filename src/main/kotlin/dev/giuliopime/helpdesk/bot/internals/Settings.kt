package dev.giuliopime.helpdesk.bot.internals

import dev.giuliopime.helpdesk.bot.HelpDesk
import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

object Settings {
    private val dotenv: Dotenv = dotenv()
    private val launchTimestamp = System.currentTimeMillis()

    var developerIDs: List<String> = getFromEnv("developers.ids").split(",")
    var testing = getFromEnv("testing") == "true"
    var logPermissionExceptions = getFromEnv("log.permission.exceptions") == "true"
    var maxCommandsNesting = getFromEnv("max.command.nesting").toInt()

    var voteTrackerApiKey = getFromEnv("vote.tracker.api.key")
    var topggApiKey = getFromEnv("topgg.api.key")

    object Discord {
        val token = getFromEnv("discord.token")
        val testToken = getFromEnv("test.discord.token")
        val status = getFromEnv("discord.status")
        val errorWebhookUrl = getFromEnv("error.webhook.url")
    }

    private fun getFromEnv(key: String): String {
        val value = dotenv[key.toUpperCase().replace('.', '_')]

        if (value == null) {
            logger.error("Couldn't find any $key key in .env file.")
            HelpDesk.instance.shutdown("Invalid .env configuration")
        }

        return value
    }

    fun getReadableUptime(): String {
        val uptime = System.currentTimeMillis() - launchTimestamp

        val seconds: Long = uptime / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        return days.toString() + " days, " + hours % 24 + " hours, " + minutes % 60 + " minutes, " + seconds % 60 + " seconds"
    }
}
