package dev.giuliopime.helpdesk.data.votetracker

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class VoteTrackerResponseD(
    val entity_id: String,
    val user_id: String,
    val source: Source,
    val timestamp: Long
)
