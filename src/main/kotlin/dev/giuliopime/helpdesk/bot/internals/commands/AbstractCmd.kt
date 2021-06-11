package dev.giuliopime.helpdesk.bot.internals.commands

import dev.giuliopime.helpdesk.bot.internals.commands.enums.BotChannelPerms
import dev.giuliopime.helpdesk.bot.internals.commands.enums.CmdCategory
import dev.giuliopime.helpdesk.bot.internals.commands.enums.CmdUserPerms

abstract class AbstractCmd(val parentCmd: AbstractCmd? = null) {
    var name: String = ""
    var aliases: List<String> = listOf()
    var description: String = ""
    var category: CmdCategory = parentCmd?.category ?: CmdCategory.DEVELOPER
    var cooldown: Long = 1000

    var requiresArgs: Boolean = false
    var usage: String = ""
    var exampleUsages: List<String> = listOf("")
    var flags: List<Pair<String, String>> = listOf()

    var uniqueUsage: Boolean = parentCmd?.uniqueUsage ?: false

    var botChannelPerms: BotChannelPerms = parentCmd?.botChannelPerms ?: BotChannelPerms.MESSAGES
    var userPerms: CmdUserPerms = parentCmd?.userPerms ?: CmdUserPerms.NONE

    var requiresHelpDeskIndex: Boolean = parentCmd?.requiresHelpDeskIndex ?: false

    fun getDefaultPath(): String {
        return if (parentCmd == null)
            name.lowercase()
        else
            (parentCmd.getDefaultPath() + "/$name").lowercase()
    }

    fun getAllPaths(): MutableList<String> {
        val paths = mutableListOf<String>()

        paths.addAll(getPossiblePaths(name))

        for (alias in aliases)
            paths.addAll(getPossiblePaths(alias))

        return paths
    }

    fun getReadablePath(): String {
        return getDefaultPath().replace("/", " ")
    }

    fun getReadableAliasesPath(): List<String> {
        val paths = mutableListOf<String>()
        val readablePath = parentCmd?.getReadablePath() ?: ""

        for (alias in aliases)
            paths.add("$readablePath $alias")

        return paths.map { it.replace("/", " ") }.toList()
    }

    private fun getPossiblePaths(topLevel: String): MutableList<String> {
        return if (parentCmd == null)
            mutableListOf(topLevel.lowercase())
        else {
            val possiblePaths = parentCmd.getAllPaths()
            for ((index, path) in possiblePaths.withIndex())
                possiblePaths[index] = "$path/$topLevel".lowercase()

            possiblePaths
        }
    }

    abstract suspend fun run(ctx: CmdCtx)
}
