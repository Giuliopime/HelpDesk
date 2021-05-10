package dev.giuliopime.helpdesk.database

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoDatabase
import dev.giuliopime.helpdesk.bot.internals.Settings
import mu.KotlinLogging
import org.litote.kmongo.KMongo

private val logger = KotlinLogging.logger {}

object MongoDBClient {
    private val dbName: String = Settings.getFromEnv("db.name")
    private val dbHost: String = Settings.getFromEnv("db.host")
    private val dbPort: String = Settings.getFromEnv("db.port")

    private val client: MongoClient = KMongo.createClient("mongodb://$dbHost:$dbPort")
    val db: MongoDatabase = client.getDatabase(dbName)

    fun shutdown() {
        client.close()
        logger.info("MongoDB client shutdown")
    }
}
