package dev.giuliopime.helpdesk.timeseriesDB

import com.influxdb.client.InfluxDBClient
import com.influxdb.client.InfluxDBClientFactory
import dev.giuliopime.helpdesk.bot.internals.Settings
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

object InfluxClient {
    private val token: String = Settings.getFromEnv("influx.token")
    private val url: String = Settings.getFromEnv("influx.url")
    private val org: String = Settings.getFromEnv("influx.org")
    val bucket: String = Settings.getFromEnv("influx.bucket")

    val client: InfluxDBClient = InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket)

    fun shutdown() {
        client.close()
        logger.info("InfluxDB client shutdown")
    }
}

