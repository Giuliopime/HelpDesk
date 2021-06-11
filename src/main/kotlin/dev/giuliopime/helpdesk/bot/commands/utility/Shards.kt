package dev.giuliopime.helpdesk.bot.commands.utility

import dev.giuliopime.helpdesk.bot.HelpDesk
import dev.giuliopime.helpdesk.bot.internals.commands.AbstractCmd
import dev.giuliopime.helpdesk.bot.internals.commands.CmdCtx
import dev.giuliopime.helpdesk.bot.internals.commands.enums.CmdCategory
import dev.giuliopime.helpdesk.bot.internals.frontend.Reactions
import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import java.time.Instant
import java.util.*

class Shards: AbstractCmd() {
    init {
        name = "shards"
        aliases = listOf("shardsInfo", "shard")
        description = "Gives you an overview of all Help Desk's shards."
        category = CmdCategory.UTILITY
        cooldown = 5000
    }

    override suspend fun run(ctx: CmdCtx) {
        val shardsManager = HelpDesk.shardsManager

        val embed = EmbedBuilder()
            .setColor(ctx.color)
            .setTitle("Help Desk Shards Info")
            .setDescription("Current shard = `${ctx.guild.jda.shardInfo.shardId}`" +
                    "\nTotal shards = `${shardsManager.shards.size}`" +
                    "\n\n**Here is a list of all the current Help Desk shards with some infos.**")
            .setFooter("If you need support use ${ctx.prefix}support")
            .setTimestamp(Instant.now())

        shardsManager.shards.forEach { shard ->
            val ping = shard.restPing.await()
            val pingEmoji = if (shard.status != JDA.Status.CONNECTED) Reactions.Extended.dnd
                else if (ping < 300) Reactions.Extended.online
                else Reactions.Extended.idle

            embed.addField(
                "$pingEmoji Shard ${shard.shardInfo.shardId}",
                "• Servers = `${shard.guilds.size}`" +
                        "\n• Cached users = `${shard.users.size}`" +
                        "\n• Status = `${shard.status.name.lowercase()
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}`" +
                        "\n• Ping = `$ping ms`",
                true
            )
        }

        ctx.respond(embed.build())
    }
}
