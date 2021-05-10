package dev.giuliopime.helpdesk.bot.internals.commands

object UsageCache {
    private val commandsMap: MutableMap<String, MutableSet<String>> = mutableMapOf()

    fun executedCommand(guildID: String, command: AbstractCmd): Boolean {
        return if (commandsMap[guildID]?.contains(command.getDefaultPath()) == true)
            false
        else {
            if (commandsMap.containsKey(guildID))
                commandsMap[guildID]?.add(command.getDefaultPath())
            else
                commandsMap[guildID] = mutableSetOf(command.getDefaultPath())

            true
        }
    }

    fun terminatedCommand(guildID: String, command: AbstractCmd) {
        commandsMap[guildID]?.remove(command.getDefaultPath())
    }
}
