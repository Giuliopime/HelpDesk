package dev.giuliopime.helpdesk.utils

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.giuliopime.helpdesk.bot.internals.Settings
import dev.giuliopime.helpdesk.data.topgg.TopggStatsD
import dev.giuliopime.helpdesk.data.votetracker.Source
import dev.giuliopime.helpdesk.data.votetracker.VoteTrackerResponseD
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

object ApisConsumer {
    private val client = HttpClient(CIO)  {
        install(JsonFeature) {
            serializer = JacksonSerializer() {
                enable(SerializationFeature.INDENT_OUTPUT)
            }
        }
    }

    private val jsonMapper = jacksonObjectMapper()

    // Unused atm
    suspend fun hasUserVoted24(userID: String, source: Source = Source.TOPGG): Boolean {
        try {
            val response: HttpResponse = client.get("https://api.votetracker.bot/v1/bot/715621848489918495/votes/${source.reqName}/$userID") {
                headers {
                    append(HttpHeaders.Authorization, Settings.voteTrackerApiKey)
                }
            }

            val data: VoteTrackerResponseD = jsonMapper.readValue(response.readText())

            return System.currentTimeMillis() - data.timestamp <= 86400000
        } catch (e: ClientRequestException) {
            return when(e.response.status) {
                HttpStatusCode.NotFound -> false
                HttpStatusCode.Unauthorized -> {
                    logger.error("Unauthorized request to Vote Tracker API", e)
                    WebhooksService.sendErrorWebhook(e)
                    true
                }
                HttpStatusCode.BadRequest -> {
                    logger.error("Bad request to Vote Tracker API", e)
                    WebhooksService.sendErrorWebhook(e)
                    true
                }
                else -> {
                    logger.error("Bad response from Vote Tracker APIs, rate limited?", e)
                    WebhooksService.sendErrorWebhook(e)
                    true
                }
            }
        } catch (e: Exception) {
            logger.error("Failed checking votes for a user", e)
            WebhooksService.sendErrorWebhook(e)
            return true
        }
    }

    suspend fun postServerCount(stats: TopggStatsD) {
        try {
            client.post<Unit>("https://top.gg/api/bots/715621848489918495/stats") {
                headers {
                    append(HttpHeaders.Authorization, Settings.topggApiKey)
                }
                contentType(ContentType.Application.Json)
                body = stats
            }
            logger.info("Posted server & shards count to top.gg")
        } catch (e: Exception) {
            logger.error("Failed posting server && shards count to top.gg", e)
            WebhooksService.sendErrorWebhook(e)
        }
    }

    fun shutdown() {
        client.close()
    }
}

