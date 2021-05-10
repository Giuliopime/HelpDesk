package dev.giuliopime.helpdesk.data.helpdesk

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SpecialQuestionD(
    val question: String,
    val emoji: String,
    val roleID: String,
)
