package dev.giuliopime.helpdesk.bot.internals.commands.enums

import net.dv8tion.jda.api.Permission

enum class BotChannelPerms(val discordPermissions: List<Permission>) {
    MESSAGES(listOf(
        Permission.VIEW_CHANNEL,
        Permission.MESSAGE_READ,
        Permission.MESSAGE_WRITE,
        Permission.MESSAGE_EMBED_LINKS,
        Permission.MESSAGE_EXT_EMOJI)),
    MESSAGES_CONTROL(listOf(
        *MESSAGES.discordPermissions.toTypedArray(),
        Permission.MESSAGE_ADD_REACTION,
        Permission.MESSAGE_MANAGE,
        Permission.MESSAGE_HISTORY,
        Permission.MESSAGE_ATTACH_FILES
    )),
    MANAGE_HELP_DESK(listOf(
        *MESSAGES_CONTROL.discordPermissions.toTypedArray(),
        Permission.MANAGE_CHANNEL,
        Permission.MANAGE_PERMISSIONS,
    ))
}
