package dev.giuliopime.helpdesk.bot.commands.guild

import dev.giuliopime.helpdesk.bot.internals.commands.AbstractCmd
import dev.giuliopime.helpdesk.bot.internals.commands.CmdCtx
import dev.giuliopime.helpdesk.bot.internals.commands.enums.CmdCategory
import dev.giuliopime.helpdesk.timeseriesDB.controllers.GuildStatsController
import dev.minn.jda.ktx.Embed

class GuildStats: AbstractCmd() {
    init {
        name = "guildStats"
        aliases = listOf("gStats", "gStat", "guildStat")
        description = "Gives you some statistics about Help Desk's usage in the server."
        cooldown = 5000
        category = CmdCategory.GUILD
    }

    override suspend fun run(ctx: CmdCtx) {
        val stats = GuildStatsController.getStats(ctx.guildID)

        val mostUsedCommands = mutableMapOf<String, Int>()
        stats.commands.forEach {
            mostUsedCommands[it.value as String] = mostUsedCommands[it.value as String]?.plus(1) ?: 1
        }
        mostUsedCommands.toSortedMap()

        val mostUsedHelpDesks = mutableMapOf<String, Int>()
        stats.questions.forEach {
            mostUsedHelpDesks[it.value as String] = mostUsedHelpDesks[it.value as String]?.plus(1) ?: 1
        }
        mostUsedHelpDesks.toSortedMap()

        val embed = Embed {
            color = ctx.color.rgb
            title = "Stats for ${ctx.guild.name}"
            thumbnail = ctx.guild.iconUrl
            description = "These are all the stats that Help Desk gathered about this server in the **last 7 days**!" +
                    "\n\n__**Help Desks stats**__" +
                    "\n• Questions answered: `${stats.questions.size}`" +
                    "\n• Most used Help Desks (message IDs): ${if(mostUsedHelpDesks.isEmpty()) "None" else mostUsedHelpDesks.toList().take(5).joinToString(", ") { "`${it.first}`" }
                    }" +
                    "\n\n__**Commands stats**__" +
                    "\n• Commands used: `${stats.commands.size}`" +
                    "\n• Most used commands: ${
                        mostUsedCommands.toList().take(5).joinToString(", ") { "`${it.first.replace("/", " ")}`" }
                    }"
        }

        ctx.respond(embed)
    }
}
