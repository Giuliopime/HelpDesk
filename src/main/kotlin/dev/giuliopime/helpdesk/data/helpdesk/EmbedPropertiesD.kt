package dev.giuliopime.helpdesk.data.helpdesk

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class EmbedPropertiesD(
    val title: String? = null,
    val titleURL: String? = null,
    val color: String = "#FFFFFF",
    val author: String? = null,
    val authorURL: String? = null,
    val authorIcon: String? = null,
    val description: String? = null,
    val fields: MutableList<Pair<String, String>> = mutableListOf(),
    val footer: String? = null,
    val footerIcon: String? = null,
    val timestamp: String? = null,
)
