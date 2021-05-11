package dev.giuliopime.helpdesk.data.influx

import com.influxdb.query.FluxRecord

data class GuildStatsD(val questions: List<FluxRecord>, val commands: List<FluxRecord>)
