package dev.giuliopime.helpdesk.bot.internals.frontend

object URLs {
    const val baseURL = "https://helpdesk.giuliopime.dev"
    const val github = "$baseURL/github"
    const val invite = "$baseURL/invite"
    const val support = "$baseURL/support"
    const val vote = "$baseURL/vote"

    fun msgLink(guildID: String, channelID: String, messageID: String): String {
        return "https://discord.com/channels/$guildID/$channelID/$messageID"
    }
}
