package dev.giuliopime.helpdesk

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import dev.giuliopime.helpdesk.bot.HelpDesk
import dev.giuliopime.helpdesk.cache.RedisClient
import dev.giuliopime.helpdesk.database.MongoDBClient
import dev.giuliopime.helpdesk.timeseriesDB.InfluxClient
import org.slf4j.LoggerFactory

fun main() {
    val loggerContext: LoggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
    loggerContext.getLogger("org.mongodb.driver").level = Level.WARN

    HelpDesk()
    MongoDBClient
    RedisClient
    InfluxClient
}
