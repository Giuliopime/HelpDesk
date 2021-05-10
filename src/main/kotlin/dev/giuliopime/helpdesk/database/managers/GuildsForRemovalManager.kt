package dev.giuliopime.helpdesk.database.managers

import dev.giuliopime.helpdesk.data.guild.GuildD
import dev.giuliopime.helpdesk.data.guild.GuildForRemovalD
import dev.giuliopime.helpdesk.database.MongoDBClient
import dev.giuliopime.helpdesk.exceptions.DBOperationFailedException
import org.litote.kmongo.`in`
import org.litote.kmongo.eq
import org.litote.kmongo.getCollection
import org.litote.kmongo.save

object GuildsForRemovalManager {
    private val guildsColl = MongoDBClient.db.getCollection<GuildForRemovalD>("guildsForRemoval")

    @Throws(DBOperationFailedException::class)
    fun add(guildForRemoval: GuildForRemovalD) {
        try {
            guildsColl.save(guildForRemoval)
        } catch (e: Exception) {
            throw DBOperationFailedException("An error occurred while deleting a guild from the database", e)
        }
    }

    fun getAll(): MutableList<GuildForRemovalD> {
        return guildsColl.find().toMutableList()
    }

    @Throws(DBOperationFailedException::class)
    fun deleteAll(guildIDs: List<String>) {
        try {
            guildsColl.deleteMany(GuildD::guildID `in`(guildIDs))
        } catch (e: Exception) {
            throw DBOperationFailedException("An error occurred while deleting a guild from the database", e)
        }
    }

    @Throws(DBOperationFailedException::class)
    fun deleteOne(guildID: String) {
        try {
            guildsColl.deleteOne(GuildD::guildID eq guildID)
                ?: throw DBOperationFailedException("Couldn't delete guild for removal with ID $guildID (NOT FOUND)")
        } catch (e: Exception) {
            throw DBOperationFailedException("An error occurred while deleting a guild from the database", e)
        }
    }
}
