package dev.giuliopime.helpdesk.bot.commands.help_desk.sub.edit_sub

import dev.giuliopime.helpdesk.bot.commands.help_desk.sub.Edit
import dev.giuliopime.helpdesk.bot.internals.commands.AbstractCmd
import dev.giuliopime.helpdesk.bot.internals.commands.CmdCtx
import dev.giuliopime.helpdesk.bot.internals.commands.CommandsHandler
import dev.giuliopime.helpdesk.bot.internals.extentions.awaitMessageOrNull
import dev.giuliopime.helpdesk.bot.internals.extentions.awaitSpecificMessage
import dev.giuliopime.helpdesk.bot.internals.extentions.takeFirstN
import dev.giuliopime.helpdesk.bot.internals.frontend.Embeds
import dev.giuliopime.helpdesk.bot.internals.frontend.Reactions
import dev.giuliopime.helpdesk.cache.handlers.GuildsHandler
import dev.minn.jda.ktx.Embed
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class AnswersMessage:AbstractCmd(Edit()) {
    init {
        name = "answersMessage"
        aliases = listOf("am", "aMsg", "aMessage")
        description = "Allows you to tweak the answers message style."
    }

    override suspend fun run(ctx: CmdCtx) {
        val hdAnswer = ctx.guildData.helpDesks[ctx.helpDeskIndex].answerEmbedProperties

        val embed = Embed {
            color = ctx.color.rgb
            title = "${Reactions.Extended.edit} Help Desk Answers Message Editor"
            description = "\n• `Author` = ${hdAnswer.author?.takeFirstN(20) ?: "Not set"}" +
                    "\n• `Author URL` = ${hdAnswer.authorURL?.takeFirstN(20) ?: "Not set"}" +
                    "\n• `Author icon` = ${hdAnswer.authorIcon?.takeFirstN(20) ?: "Not set"}" +
                    "\n\n• `Title` = ${hdAnswer.title?.takeFirstN(20) ?: "Not set"}" +
                    "\n• `Title URL` = ${hdAnswer.titleURL?.takeFirstN(20) ?: "Not set"}" +
                    "\n• `Thumbnail` = ${hdAnswer.thumbnail?.takeFirstN(20) ?: "Not set"}" +
                    "\n• `Image` = ${hdAnswer.image?.takeFirstN(20) ?: "Not set"}" +
                    "\n\n• `Footer` = ${hdAnswer.footer?.takeFirstN(20) ?: "Not set"}" +
                    "\n• `Footer Icon` = ${hdAnswer.footerIcon?.takeFirstN(20) ?: "Not set"}" +
                    "\n• `Timestamp` = ${hdAnswer.timestamp?.takeFirstN(20) ?: "Not set"}"
            field {
                name = "\u200b"
                value = "__**To change a value send its name (or an abbreviation) after this message.**__" +
                        "\nExamples:\n• `footer`\n• `thumb`\n\n*Send `done` to exit this editor.*"
                inline = false
            }
        }

        ctx.respond(embed)

        val choices = listOf(
            "Author", "Author URL", "Author icon", "Title", "Title URL",
            "Thumbnail", "Image", "Footer", "Footer Icon", "Timestamp", "done"
        )

        val choice = ctx.channel.awaitSpecificMessage(ctx.userID, choices, 60000)

        when (choice) {
            null ->  {
                ctx.respond(Embeds.operationFailed("60 seconds time limit exceed.", "Next time send a value within 60 seconds."))
                return
            }
            "done" -> {
                ctx.cmd = CommandsHandler.getCommand(parentCmd!!.getDefaultPath())
                GlobalScope.async {
                    CommandsHandler.runCmd(ctx)
                }
                return
            }
        }

        ctx.args = mutableListOf(choice!!)
        ctx.cmd = CommandsHandler.getCommand(getDefaultPath() + "/handleAnswerProperty")
        GlobalScope.async {
            CommandsHandler.runCmd(ctx)
        }
    }

    class HandleAnswerProperty:AbstractCmd(AnswersMessage()) {
        init {
            name = "handleAnswerProperty"
            aliases = listOf("hap")
            description = "Used to handle the editing / deletion of a property of the help desk answers message."
            requiresArgs = true
            usage = "[property_name || delete]"
            exampleUsages = listOf("image", "footer", "delete")
            cooldown = 2000
        }

        override suspend fun run(ctx: CmdCtx) {
            val property = ctx.args.first().lowercase().replace(" ", "_")

            val charLimit = when (property) {
                "author" -> 256
                "author_url" -> 200
                "author_icon" -> 200
                "title" -> 256
                "title_url" -> 200
                "thumbnail" -> 200
                "image" -> 200
                "footer" -> 2048
                "footer_icon" -> 200
                "timestamp" -> 20
                "delete" -> 10
                else -> -1
            }

            if (charLimit == -1) {
                ctx.respond(Embeds.operationFailed(
                    "You didn't provide a valid property of the Help Desk message.",
                    "Reuse the command and provide one of the following properties:" +
                            "\n• `author`" +
                            "\n• `author url`" +
                            "\n• `author icon`" +
                            "\n• `title`" +
                            "\n• `title url`" +
                            "\n• `thumbnail`" +
                            "\n• `image`" +
                            "\n• `footer`" +
                            "\n• `footer icon`" +
                            "\n• `timestamp`" +
                            "\n\n• `delete` to delete the property content"
                ))
                return
            }

            // TODO: Add documentation link for property guide
            ctx.respond(Embed {
                color = ctx.color.rgb
                description = "Send the content for the property `$property` after this message." +
                        "\n*Send `delete` instead to delete this property content.*" +
                        "\n\n**This property has a character limit of $charLimit.**"
            })

            val valueMsg = ctx.channel.awaitMessageOrNull({
                it.author.id == ctx.userID && it.message.contentRaw.length <= charLimit
            })

            if (valueMsg == null) {
                ctx.respond(Embeds.operationFailed("60 seconds time limit exceed.", "Next time send a value within 60 seconds."))
                return
            }

            val value = valueMsg.contentRaw
            val propertyValue = if (value.lowercase() == "delete") null else value

            val dbPropertyName = when(property) {
                "author" -> "author"
                "author_url" -> "authorURL"
                "author_icon" -> "authorIcon"
                "title" -> "title"
                "title_url" -> "titleURL"
                "thumbnail" -> "thumbnail"
                "image" -> "image"
                "footer" -> "footer"
                "footer_icon" -> "footerIcon"
                else -> "timestamp"
            }

            ctx.guildData = GuildsHandler.updateWithRoute(ctx.guildID, "helpDesks.${ctx.helpDeskIndex}.answerEmbedProperties.$dbPropertyName", propertyValue)

            ctx.cmd = CommandsHandler.getCommand(parentCmd!!.getDefaultPath())
            GlobalScope.async {
                CommandsHandler.runCmd(ctx)
            }
        }
    }
}
