package dev.giuliopime.helpdesk.bot.commands.utility

import dev.giuliopime.helpdesk.bot.internals.commands.AbstractCmd
import dev.giuliopime.helpdesk.bot.internals.commands.CmdCtx
import dev.giuliopime.helpdesk.bot.internals.commands.CommandsHandler
import dev.giuliopime.helpdesk.bot.internals.commands.enums.BotChannelPerms
import dev.giuliopime.helpdesk.bot.internals.commands.enums.CmdCategory
import dev.giuliopime.helpdesk.bot.internals.commands.enums.CmdUserPerms
import dev.minn.jda.ktx.Embed
import net.dv8tion.jda.api.EmbedBuilder

class Help: AbstractCmd() {
    init {
        name = "help"
        description = "Gives some useful information to get started using Help Desk and a list of its commands."
        usage = "(command name)"
        exampleUsages = listOf("", "prefix")
        category = CmdCategory.UTILITY
    }
    override suspend fun run(ctx: CmdCtx) {
        val prefix = ctx.prefix
        val baseURL = "https://helpdesk.giuliopime.dev"

        if (ctx.args.size > 0 && CommandsHandler.getCommandOrNull(ctx.args.joinToString("/").toLowerCase()) != null) {
            val cmd = CommandsHandler.getCommand(ctx.args.joinToString("/").toLowerCase())
            val cmdNamePath = cmd.getReadablePath()

            val description = StringBuilder().append("**Description**" +
                    "\n${cmd.description}")

            if (cmd.parentCmd != null)
                description.append("\n\n**Parent command:** `${cmd.parentCmd.getReadablePath()}`")

            if (cmd.aliases.isNotEmpty())
                description.append("\n\n**Aliases:** `${cmd.aliases.joinToString("`, `") { "${cmd.parentCmd?.getReadablePath()?.plus(" ") ?: ""}$it" }}`\n${if (cmd.parentCmd != null) "(*You can combine these aliases with the parent command ones*)" else ""}")

            val usage = StringBuilder()
            usage.append("```" +
                    "\n${prefix + cmdNamePath + " " + cmd.usage}" +
                    "\n```")

            // TODO: Add links to documentation
            val permissions = StringBuilder()
            permissions.append("• Help Desk required permissions: ")
            permissions.append(when(cmd.botChannelPerms) {
                BotChannelPerms.MESSAGES -> "`Messages`"
                BotChannelPerms.MESSAGES_CONTROL -> "`Messages Control`"
                else -> "`Manage help-desk`"
            })

            permissions.append("\n• User required permissions: ")
            permissions.append(when(cmd.userPerms) {
                CmdUserPerms.NONE -> "*None*"
                CmdUserPerms.MANAGE_CHANNELS -> "`Manage Channels`"
                CmdUserPerms.ADMINISTRATOR -> "`Administrator`"
            })

            val notes = StringBuilder()
            if (cmd.cooldown != 1000L)
                notes.append("\n• *${cmd.cooldown/1000} seconds cooldown*")

            val embed = EmbedBuilder()
                .setColor(ctx.color)
                .setAuthor("Command Help: $cmdNamePath", null, ctx.guild.jda.selfUser.effectiveAvatarUrl)
                .setDescription(description)
                .addField(
                    "Usage",
                    "$usage" +
                            "\n• Arguments inside `[]` parenthesis are mandatory" +
                            "\n• Arguments inside `()` parenthesis are optional" +
                            "\n• `||` means `or`",
                    false
                )


            if (cmd.flags.isNotEmpty()) {
                embed.addField(
                    "Flags",
                    "```\n${cmd.flags.joinToString("\n") { "${it.first} • ${it.second}" }}\n```",
                    false
                )
            }

            embed.addField(
                "Examples",
                "```\n${cmd.exampleUsages.joinToString("\n") { "$prefix$cmdNamePath $it"}}\n```",
                false
            )

            embed.addField(
                "\u200b\nPermissions",
                permissions.toString(),
                false
            )

            if (notes.toString() != "")
                embed.addField(
                    "Notes & Restrictions",
                    notes.toString(),
                    false
                )

            embed.addField(
                "\u200b",
                "**[Invite]($baseURL/invite)** | **[Documentation]($baseURL)** | **[Support]($baseURL/support)** | **[GitHub]($baseURL/github)**",
                false
            )

            ctx.respond(embed.build())
        } else {

            ctx.respond(Embed {
                author {
                    name = "Help Interface"
                    url = "https://helpdesk.giuliopime.dev"
                    iconUrl = ctx.guild.jda.selfUser.effectiveAvatarUrl
                }
                color = ctx.color.rgb
                description = "• Prefix for this server = `$prefix`" +
                        "\n• Look up a specific command with `${prefix}help command name`" +
                        "\n" +
                        "• Learn how to use Help Desk's commands in the fastest and most efficient way [`here`]($baseURL)." +
                        "\n\n**Here is a full list of all Help Desk's commands:**\n\u200b\n"
                field {
                    name = "Server Settings"
                    value = "```yaml" +
                            "\n${
                                CommandsHandler.getCommandsFromCategory(CmdCategory.GUILD)
                                    .joinToString("\n") { "- " + it.getReadablePath() }
                            }" +
                            "\n```"
                    inline = false
                }

                field {
                    name = "Utility"
                    value = "```yaml" +
                            "\n${
                                CommandsHandler.getCommandsFromCategory(CmdCategory.UTILITY)
                                    .joinToString("\n") { "- " + it.getReadablePath() }
                            }" +
                            "\n```"
                    inline = false
                }
                field {
                    name = "Help Desk"
                    value = "```yaml" +
                            "\n${
                                CommandsHandler.getCommandsFromCategory(CmdCategory.HELP_DESK)
                                    .joinToString("\n") { "- " + it.getReadablePath() }
                            }" +
                            "\n```"
                    inline = false
                }
                field {
                    name = "\u200b"
                    value = "**[Invite]($baseURL/invite)** | **[Documentation]($baseURL)** | **[Support]($baseURL/support)** | **[GitHub]($baseURL/github)**"
                    inline = false

                }

                footer {
                    name = "Thank you for choosing Help Desk ;)"
                }
            })
        }
    }
}
