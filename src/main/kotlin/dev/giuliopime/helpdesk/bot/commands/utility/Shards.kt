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

        shardsManager.shards.forEach {
            val ping = it.restPing.await()
            val pingEmoji = if (it.status != JDA.Status.CONNECTED) Reactions.Extended.dnd
                else if (ping < 300) Reactions.Extended.online
                else Reactions.Extended.idle

            embed.addField(
                "$pingEmoji Shard ${it.shardInfo.shardId}",
                "• Servers = `${it.guilds.size}`" +
                        "\n• Cached users = `${it.users.size}`" +
                        "\n• Status = `${it.status.name.toLowerCase().capitalize()}`" +
                        "\n• Ping = `$ping ms`",
                true
            )
        }

        ctx.respond(embed.build())
    }
}
