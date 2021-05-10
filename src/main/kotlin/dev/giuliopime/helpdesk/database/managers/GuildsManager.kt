package dev.giuliopime.helpdesk.database.managers

import com.mongodb.client.model.FindOneAndReplaceOptions
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import dev.giuliopime.helpdesk.data.guild.GuildD
import dev.giuliopime.helpdesk.database.MongoDBClient
import org.litote.kmongo.*
import dev.giuliopime.helpdesk.exceptions.DBOperationFailedException

object GuildsManager {
    private val guildsColl = MongoDBClient.db.getCollection<GuildD>("guilds")

    fun exists(guildID: String): Boolean {
        return guildsColl.findOne(GuildD::guildID eq guildID) != null
    }

    @Throws(DBOperationFailedException::class)
    fun getOrCreate(guildID: String): GuildD {
        var guildD: GuildD? = guildsColl.findOne(GuildD::guildID eq guildID)

        if (guildD == null) {
            guildD = GuildD(guildID)
            try {
                guildsColl.save(guildD)
            } catch (e: Exception) {
                throw DBOperationFailedException("An error occurred while saving a new guild to the database", e)
            }
        }

        return guildD
    }

    @Throws(DBOperationFailedException::class)
    fun getOrNull(guildID: String): GuildD? {
        return guildsColl.findOne(GuildD::guildID eq guildID)
    }

    @Throws(DBOperationFailedException::class)
    fun update(guildData: GuildD): GuildD {
        return guildsColl.findOneAndReplace(
            GuildD::guildID eq guildData.guildID,
            guildData,
            FindOneAndReplaceOptions().returnDocument(ReturnDocument.AFTER)
        ) ?: throw DBOperationFailedException("Couldn't update guild with ID ${guildData.guildID} (NOT FOUND)")
    }

    @Throws(DBOperationFailedException::class)
    fun updateWithRoute(guildID: String, route: String, value: Any?): GuildD {
        return guildsColl.findOneAndUpdate(
            "{ guildID: ${guildID.json} }",
            "{ \$set: { \"$route\": ${value?.json} } }",
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        ) ?: throw DBOperationFailedException("Couldn't update guild with ID $guildID (NOT FOUND)")
    }

    @Throws(DBOperationFailedException::class)
    fun delete(guildID: String) {
        try {
            val deleted = guildsColl.deleteOne(GuildD::guildID eq guildID).deletedCount > 0
            if (!deleted)
                throw DBOperationFailedException("Couldn't delete guild with ID $guildID (NOT FOUND)")
        } catch (e: Exception) {
            throw DBOperationFailedException("An error occurred while deleting a guild from the database", e)
        }
    }

    fun deleteAll(guildIDs: List<String>) {
        try {
            guildsColl.deleteMany(GuildD::guildID `in`(guildIDs))
        } catch (e: Exception) {
            throw DBOperationFailedException("An error occurred while deleting a guild from the database", e)
        }
    }
}
