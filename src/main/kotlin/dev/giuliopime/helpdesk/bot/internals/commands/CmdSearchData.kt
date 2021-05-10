package dev.giuliopime.helpdesk.bot.internals.commands

data class CmdSearchData(
    val cmd: AbstractCmd,
    val args: MutableList<String> = mutableListOf(),
    val flags: MutableList<String> = mutableListOf()
)
