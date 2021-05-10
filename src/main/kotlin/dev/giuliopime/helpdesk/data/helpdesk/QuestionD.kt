package dev.giuliopime.helpdesk.data.helpdesk

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class QuestionD(
    val question: String,
    val reply: String,
    val emoji: String,
)
