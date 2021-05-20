package dev.giuliopime.helpdesk.bot.commands.help_desk.sub.edit_sub.questions

import dev.giuliopime.helpdesk.bot.commands.help_desk.sub.edit_sub.Questions
import dev.giuliopime.helpdesk.bot.internals.commands.AbstractCmd
import dev.giuliopime.helpdesk.bot.internals.commands.CmdCtx
import dev.giuliopime.helpdesk.bot.internals.commands.CommandsHandler
import dev.giuliopime.helpdesk.bot.internals.commands.enums.BotChannelPerms
import dev.giuliopime.helpdesk.bot.internals.extentions.awaitMessageOrNull
import dev.giuliopime.helpdesk.bot.internals.extentions.getRole
import dev.giuliopime.helpdesk.bot.internals.frontend.Colors
import dev.giuliopime.helpdesk.bot.internals.frontend.Embeds
import dev.giuliopime.helpdesk.bot.internals.frontend.Reactions
import dev.giuliopime.helpdesk.cache.handlers.GuildsHandler
import dev.minn.jda.ktx.Embed
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import net.dv8tion.jda.api.EmbedBuilder

class EditRole: AbstractCmd(Questions.HandleQuestion()) {
    init {
        name = "editRole"
        aliases = listOf("erole")
        description = "Can be used to edit the role that Help Desk will assign to the user who uses a specific question of the Help Desk."
        requiresArgs = true
        usage = "[question number]"
        exampleUsages = listOf("3", "20")
        cooldown = 2000
    }

    override suspend fun run(ctx: CmdCtx) {
        val index = ctx.args.first().toIntOrNull()?.minus(1)

        val questions = ctx.guildData.helpDesks[ctx.helpDeskIndex].questions
        val maxIndex = if (questions.size == 20) 20 else questions.size + 1

        if (index == null || index < 0 || index > maxIndex) {
            ctx.respond(Embeds.operationFailed("You didn't provide a valid number.", "Reuse the command and provide a number between 1 and $maxIndex."))
            return
        }

        ctx.respond(Embed {
            color = ctx.color.rgb
            description = "**Send the role that should get assigned to users who use this question of the Help Desk.**\n" +
                    "*Send `delete` instead to unset the role.*"
        })

        val msg = ctx.channel.awaitMessageOrNull({
            it.author.id == ctx.userID && (it.message.contentRaw.toLowerCase() == "delete" || (it.message.getRole() != null && it.message.getRole()!!.guild.id == ctx.guildID))
        })

        if (msg == null) {
            ctx.respond(
                Embeds.operationFailed(
                    "60 seconds time limit exceeded.",
                    "Next time send a valid message within 60 seconds."
                )
            )
            return
        }

        val role = if (msg.contentRaw.toLowerCase() == "delete") null else msg.getRole()

        if (role != null && !ctx.guild.selfMember.canInteract(role)) {
            ctx.respond(Embeds.operationFailed(
                "The provided role is not manageable by Help Desk, meaning I can't assign it to users.",
                "Make sure that the role you want to provide is below the Help Desk role in the *Server Roles settings*." +
                        "\nFind out more about roles hierarchy [here](https://support.discord.com/hc/en-us/articles/214836687-Role-Management-101)."
            ))
            return
        }

        ctx.guildData = GuildsHandler.updateWithRoute(ctx.guildID, "helpDesks.${ctx.helpDeskIndex}.questions.$index.roleID", role?.id)

        ctx.cmd = CommandsHandler.getCommand(parentCmd!!.getDefaultPath())
        GlobalScope.async {
            CommandsHandler.runCmd(ctx)
        }
    }
}
