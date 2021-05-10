package dev.giuliopime.helpdesk.bot.internals.commands.enums

import net.dv8tion.jda.api.Permission

enum class BotServerPerms(val discordPermissions: List<Permission>) {
    NONE(listOf()),
    MANAGE_HELPDESKS(listOf(
        *BotChannelPerms.MESSAGES_CONTROL.discordPermissions.toTypedArray(),
        Permission.MANAGE_CHANNEL,
        Permission.MANAGE_PERMISSIONS,
    ))
}
