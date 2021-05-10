package dev.giuliopime.helpdesk.timeseriesDB.controllers

import com.influxdb.client.write.Point
import com.influxdb.query.FluxRecord
import com.influxdb.query.FluxTable
import dev.giuliopime.helpdesk.data.influx.GuildStatsD
import dev.giuliopime.helpdesk.timeseriesDB.InfluxClient
import net.dv8tion.jda.api.entities.Guild

object GuildStatsController {
    private val client = InfluxClient.client
    private val bucket = InfluxClient.bucket

    private val queryApi = client.queryApi
    private val writeApi = client.writeApi

    fun getStats(guildID: String, lookback: String = "-7d"): GuildStatsD {
        return GuildStatsD()
    }

    fun writeGuildJoin(guild: Guild) {
        val point = Point("guildsjoined")
            .addField("guild_id", guild.id)
            .addField("members", guild.memberCount)

        writeApi.writePoint(point)
    }

    fun writeGuildLeft(guild: Guild) {
        val point = Point("guildsleft")
            .addField("guild_id", guild.id)
            .addField("members", guild.memberCount)

        writeApi.writePoint(point)
    }

    fun writeTotalGuilds(totalGuilds: Long) {
        val point = Point("guilds")
            .addField("guilds_count", totalGuilds)

        writeApi.writePoint(point)
    }

    fun writeCommand(commandName: String, userID: String, guildID: String) {
        val point = Point("command")
            .addTag("user_id", userID)
            .addTag("guild_id", guildID)
            .addField("command_name", commandName)

        writeApi.writePoint(point)
    }

    fun writeQuestion(userID: String, guildID: String, helpDeskID: String) {
        val point = Point("vc")
            .addTag("user_id", userID)
            .addTag("guild_id", guildID)
            .addField("help_desk_id", helpDeskID)

        writeApi.writePoint(point)
    }
}
