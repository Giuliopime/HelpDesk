package dev.giuliopime.helpdesk.bot.internals.frontend

import dev.giuliopime.helpdesk.bot.internals.commands.AbstractCmd
import dev.giuliopime.helpdesk.bot.internals.commands.enums.BotChannelPerms
import dev.giuliopime.helpdesk.bot.internals.commands.enums.CmdUserPerms
import dev.giuliopime.helpdesk.data.helpdesk.HelpDeskD
import dev.minn.jda.ktx.Embed
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color
import java.time.Instant

object Embeds {
    // OPERATION RESULTS
    fun operationSuccessful(info: String): MessageEmbed = Embed {
        color = Colors.green.rgb
        title = "${Reactions.Extended.checkmark} Operation successful"
        description = info
    }

    fun operationSuccessfulBackToParentCmd(info: String, backTo: String, extendedEmoji: String): MessageEmbed = Embed {
        color = Colors.green.rgb
        title = "${Reactions.Extended.checkmark} Operation successful"
        description = info +
                "\n\nReact with $extendedEmoji to get back to $backTo"
    }

    fun operationCanceled(cause: String): MessageEmbed = Embed {
        color = Colors.green.rgb
        title = "${Reactions.Extended.checkmark} Operation canceled"
        field {
            name = "Cause"
            value = cause
        }
    }

    fun operationFailed(cause: String, solution: String): MessageEmbed = Embed {
        color = Colors.red.rgb
        title = "${Reactions.Extended.error} Operation failed"
        field {
            name = "Cause"
            value = cause
            inline = false
        }
        field {
            name = "Solution"
            value = solution
            inline = false
        }
    }



    // COMMANDS
    fun incorrectUsage(prefix: String, cmd: AbstractCmd): MessageEmbed {
        val cmdName = prefix + cmd.getReadablePath()

        val exampleUsages = StringBuilder()
        cmd.exampleUsages.forEach { exampleUsages.append("\n$cmdName $it") }

        return EmbedBuilder()
            .setColor(Colors.red)
            .setTitle("${Reactions.Extended.error} Incorrect usage of the command")
            .addField("Proper usage", "```\n$cmdName ${cmd.usage}\n```", false)
            .addField("Examples", "```$exampleUsages\n```", false)
            .addField("More info", "Use `${prefix}help ${cmdName}` for more info about this command.\nIf you are stuck check out [how to use commands](${URLs.baseURL}).", false)
            .build()
    }

    val commandAlreadyInUse = Embed {
        color = Colors.red.rgb
        title = "${Reactions.Extended.error} Command already in use"
        description = "**This command is already being used in this server.**\nStop its execution elsewhere before re-running it please."
    }

    val unknownFailure = Embed {
        color = Colors.red.rgb
        title = "${Reactions.Extended.error} Unknown issue"
        description = "An unknown issue occurred :/\nIt has already been forwarded to the developers and it's gonna get fixed soon don't worry :)\n\nYou can join the [Support Server](${URLs.support}) to stay up to date with updates and bug fixes or to ask for assistance."
        timestamp = Instant.now()
    }

    fun commandOnCooldown(cooldown: Long, userID: String, cmdName: String): MessageEmbed {
        return Embed {
            color = Colors.red.rgb
            title = "${Reactions.Extended.error} Command on cooldown"
            description = "*<@${userID}> please wait ${cooldown / 1000} more second${if (cooldown / 1000 > 1) "s" else ""} before reusing the `$cmdName` command.*"
        }
    }

    fun helpDeskChoice(helpdesks: MutableList<HelpDeskD>, guildID: String, color: Color = Colors.primary): MessageEmbed {
        val embedBuilder = EmbedBuilder()
            .setColor(color)
            .setTitle("${Reactions.Extended.question} Help Desk selection dialog")

        val descriptionBuilder = StringBuilder()
        descriptionBuilder.append("**Select an Help Desk on which you want to apply this command.**\n\nTo select an Help Desk send its index after this message.\nExample: `1`.")

        for ((index, hd) in helpdesks.withIndex())
            descriptionBuilder.append("\n\n`${index + 1}.` **<#${hd.channelID}>** [message ID: [`${hd.messageID}`](${URLs.msgLink(guildID, hd.channelID, hd.messageID)}]")

        descriptionBuilder.append("\n\n*You have 60 seconds* to send the index.\n*To cancel* this action send `cancel`.")

        embedBuilder.setDescription(descriptionBuilder.toString())

        return embedBuilder.build()
    }



    // PERMISSIONS
    fun missingBotChannelPerms(permType: BotChannelPerms): MessageEmbed {
        val description = StringBuilder()
        description.append("**Help Desk is missing the following permissions, please report it to a moderator of the server:**")
        permType.discordPermissions.forEach { description.append("\n• ${it.getName()}") }

        return EmbedBuilder()
            .setColor(Colors.red)
            .setTitle("${Reactions.Extended.error} Missing bot permissions")
            .setDescription(description.toString())
            .addField("Quick fix", "Go in the `Channel Settings`, click `Permission` in the left side menu, add `Help Desk` and assign him the permissions listed above.", false)
            .addField("Why are those permissions required?", "You can find out why Help Desk needs those permissions [here](${URLs.baseURL}).", false)
            .build()
    }


    fun missingUserPerms(permType: CmdUserPerms): MessageEmbed {
        val description = StringBuilder()
        description.append("**This command requires you to have the following permissions:**")
        permType.discordPermissions.forEach { description.append("\n• ${it.getName()}") }

        return EmbedBuilder()
            .setColor(Colors.red)
            .setTitle("${Reactions.Extended.error} Insufficient user permissions")
            .setDescription(description.toString())
            .build()
    }
}
