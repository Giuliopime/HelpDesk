package dev.giuliopime.helpdesk.data.helpdesk

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class HelpDeskD(
    val channelID: String,
    val messageID: String,
    val notification: String? = null,
    val notificationChannel: String? = null,
    val embedProperties: EmbedPropertiesD = EmbedPropertiesD(),
    val responseEmbedProperties: EmbedPropertiesD = EmbedPropertiesD(),
    val questions: MutableList<QuestionD> = mutableListOf(),
    val specialQuestion: SpecialQuestionD? = null
)
