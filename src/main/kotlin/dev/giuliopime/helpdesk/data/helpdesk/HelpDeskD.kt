package dev.giuliopime.helpdesk.data.helpdesk

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class HelpDeskD(
    var channelID: String,
    var messageID: String,
    val embedProperties: EmbedPropertiesD = EmbedPropertiesD(),
    val answerEmbedProperties: EmbedPropertiesD = EmbedPropertiesD(),
    val questions: MutableList<QuestionD> = mutableListOf(),
)
