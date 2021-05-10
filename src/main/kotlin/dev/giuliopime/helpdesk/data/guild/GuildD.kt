package dev.giuliopime.helpdesk.data.guild

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import dev.giuliopime.helpdesk.data.helpdesk.HelpDeskD

@JsonIgnoreProperties(ignoreUnknown = true)
data class GuildD(
    val guildID: String,
    var prefix: String = "hd?",
    val helpDesks: MutableList<HelpDeskD> = mutableListOf()
)
