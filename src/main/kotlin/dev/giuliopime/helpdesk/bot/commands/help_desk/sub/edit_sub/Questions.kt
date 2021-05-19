package dev.giuliopime.helpdesk.bot.commands.help_desk.sub.edit_sub

import dev.giuliopime.helpdesk.bot.commands.help_desk.sub.Edit
import dev.giuliopime.helpdesk.bot.internals.commands.AbstractCmd
import dev.giuliopime.helpdesk.bot.internals.commands.CmdCtx
import dev.giuliopime.helpdesk.bot.internals.commands.CommandsHandler
import dev.giuliopime.helpdesk.bot.internals.extentions.*
import dev.giuliopime.helpdesk.bot.internals.frontend.Embeds
import dev.giuliopime.helpdesk.bot.internals.frontend.Reactions
import dev.giuliopime.helpdesk.cache.handlers.GuildsHandler
import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.await
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import java.lang.StringBuilder

class Questions: AbstractCmd(Edit()) {
    init {
        name = "questions"
        aliases = listOf("question", "qt")
        description = "Allows you to set the questions (an asnwers) of the Help Desk, as well as tweaking their reactions, role action and notification."
    }

    override suspend fun run(ctx: CmdCtx) {
        val questions = mutableListOf<String>()
        questions.addAll(ctx.guildData.helpDesks[ctx.helpDeskIndex].questions.map { it.question?.takeFirstN(25) ?: "Not set" })

        val questionsString = StringBuilder()
        questions.forEachIndexed { index, s ->
            questionsString.append("`${index + 1}.` $s\n")
        }

        if (questions.size < 20)
            questionsString.append("`${questions.size + 1}.` **Create new question**")

        val embed = Embed {
            color = ctx.color.rgb
            title = "${Reactions.Extended.edit} Questions Editor"
            description = questionsString.toString()
            field {
                name = "\u200b"
                value = "__**To add / edit / delete a question send its number in this chat.**__" +
                        "\nExample: `1`\n\n*Send `done` to get back to the Help Desk editor.*"
                inline = false
            }
        }

        ctx.respond(embed)

        val answerIndex = ctx.channel.awaitNumericMessage(ctx.userID, "done", if (questions.size == 20) 20 else questions.size + 1)

        when (answerIndex) {
            null ->  {
                ctx.respond(Embeds.operationFailed("60 seconds time limit exceed.", "Next time send a valid number within 60 seconds."))
                return
            }
            -1 -> {
                ctx.cmd = CommandsHandler.getCommand(parentCmd!!.getDefaultPath())
                GlobalScope.async {
                    CommandsHandler.runCmd(ctx)
                }
                return
            }
        }

        ctx.cmd = CommandsHandler.getCommand(getDefaultPath() + "/handleQuestion")
        ctx.args = mutableListOf(answerIndex.toString())
        GlobalScope.async {
            CommandsHandler.runCmd(ctx)
        }
    }



    class HandleQuestion: AbstractCmd(Questions()) {
        init {
            name = "handleQuestion"
            description = "Used to handle the creation / editing / deletion of a question."
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

            val question = ctx.guildData.helpDesks[ctx.helpDeskIndex].questions.getOrNull(index)

            var reaction = "Not set"
            if (question?.reaction != null) {
                reaction = if (question.reaction.toLongOrNull() != null) {
                    try {
                        ctx.guild.retrieveEmoteById(question.reaction).await().asMention
                    } catch (e: ErrorResponseException) { "Not set" } catch (e: IllegalArgumentException) { "Not set" }
                } else question.reaction
            }

            val embed = Embed {
                color = ctx.color.rgb
                title = "${Reactions.Extended.edit} Question Editor"
                field {
                    name = "Question"
                    value = question?.question?.takeFirstN(100) ?: "Not set"
                    inline = false
                }
                field {
                    name = "Answer"
                    value = question?.answer?.takeFirstN(100) ?: "Not set"
                    inline = false
                }
                field {
                    name = "Reaction"
                    value = reaction
                    inline = false
                }
                field {
                    name = "Role assignment"
                    value = if (question?.roleID != null) "<@&${question.roleID}>" else "Not set"
                    inline = false
                }
                field {
                    name = "Notification Channel"
                    value = if (question?.notificationChannel != null) "<#${question.notificationChannel}>" else "Not set"
                    inline = false
                }
                field {
                    name = "Notification Message"
                    value = question?.notificationMessage?.takeFirstN(100) ?: "Not set"
                    inline = false
                }
                field {
                    name = "\u200b"
                    value = "__**To modify a value send its name or an abbreviation.**__" +
                            "\nExamples:\n• `answer`\n• `reac`" +
                            "\n\n__**Once finished you can send:**__\n• `done` to exit\n• `delete` to delete this question"
                }
            }

            ctx.respond(embed)

            val choices = listOf("question", "answer", "reaction", "role", "notification channel", "notification message", "done", "delete")

            val choice = ctx.channel.awaitSpecificMessage(ctx.userID, choices)

            if (choice == null) {
                ctx.respond(Embeds.operationFailed("60 seconds time limit exceed.", "Next time send a value within 60 seconds."))
                return
            }

            var cmdName: String
            when (choice) {
                "question" -> cmdName = "editQuestion"
                "answer" -> cmdName = "editAnswer"
                "reaction" -> cmdName = "editReaction"
                "role" -> cmdName = "editRole"
                "notification channel" -> cmdName = "editNotiChannel"
                "notification message" -> cmdName = "editNotiMessage"
                else -> {
                    if (choice == "delete" && question != null) {
                        ctx.guildData.helpDesks[ctx.helpDeskIndex].questions.removeAt(index)
                        GuildsHandler.updateWithRoute(ctx.guildID, "helpDesks.${ctx.helpDeskIndex}.questions", ctx.guildData.helpDesks[ctx.helpDeskIndex].questions)
                    }

                    ctx.cmd = CommandsHandler.getCommand(parentCmd!!.getDefaultPath())
                    GlobalScope.async {
                        CommandsHandler.runCmd(ctx)
                    }
                    return
                }
            }

            ctx.cmd = CommandsHandler.getCommand(getDefaultPath() + "/$cmdName")
            GlobalScope.async {
                CommandsHandler.runCmd(ctx)
            }
        }
    }
}
