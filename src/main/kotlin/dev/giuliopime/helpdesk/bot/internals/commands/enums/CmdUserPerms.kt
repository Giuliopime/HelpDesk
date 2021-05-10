package dev.giuliopime.helpdesk.bot.internals.commands.enums

import net.dv8tion.jda.api.Permission

enum class CmdUserPerms(val discordPermissions: List<Permission>) {
    ADMINISTRATOR(listOf(Permission.ADMINISTRATOR)),
    MANAGE_CHANNELS(listOf(Permission.MANAGE_CHANNEL)),
    NONE(listOf())
}
