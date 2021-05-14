package dev.giuliopime.helpdesk.bot.internals.commands

import dev.giuliopime.helpdesk.bot.HelpDesk
import dev.giuliopime.helpdesk.bot.commands.guild.Config
import dev.giuliopime.helpdesk.bot.commands.guild.Prefix
import dev.giuliopime.helpdesk.bot.commands.guild.Stats
import dev.giuliopime.helpdesk.bot.commands.utility.*
import dev.giuliopime.helpdesk.bot.internals.Settings
import dev.giuliopime.helpdesk.bot.internals.commands.enums.BotChannelPerms
import dev.giuliopime.helpdesk.bot.internals.commands.enums.CmdCategory
import dev.giuliopime.helpdesk.bot.internals.commands.enums.CmdUserPerms
import dev.giuliopime.helpdesk.bot.internals.frontend.Colors
import dev.giuliopime.helpdesk.bot.internals.frontend.Embeds
import dev.giuliopime.helpdesk.cache.handlers.CooldownsHandler
import dev.giuliopime.helpdesk.data.guild.GuildD
import dev.giuliopime.helpdesk.timeseriesDB.controllers.GuildStatsController
import dev.giuliopime.helpdesk.utils.WebhooksService
import dev.minn.jda.ktx.await
import kotlinx.coroutines.withTimeoutOrNull
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import java.util.regex.Pattern

private val logger = KotlinLogging.logger {}

object CommandsHandler {
    private lateinit var commandsSet: MutableSet<AbstractCmd>
    private lateinit var commandsMap: MutableMap<String, AbstractCmd>

    init {
        loadCommands()
    }

    private fun loadCommands() {
        commandsSet = mutableSetOf(
            // Utility
            Help(),
            Info(),
            Invite(),
            Support(),
            Ping(),
            Shards(),
            Vote(),

            // Guild
            Prefix(),
            Config(),
            Stats()
        )
        commandsMap = mutableMapOf()

        for (cmd in commandsSet) {
            val cmdPaths = cmd.getAllPaths()
            for (path in cmdPaths) {
                val previous = commandsMap.put(path, cmd)
                if (previous != null) {
                    HelpDesk.instance.shutdown("Duplicate command path: $path")
                    return
                }
            }
        }
    }


    @Throws(NoSuchElementException::class)
    fun getCommand(path: String): AbstractCmd {
        return commandsMap[path.toLowerCase()]
            ?: throw NoSuchElementException("Command not found at path $path")
    }

    fun getCommandOrNull(path: String): AbstractCmd? {
        return commandsMap[path]
    }

    fun getCommandsFromCategory(category: CmdCategory): List<AbstractCmd> {
        return commandsSet.filter { it.category == category }.toList()
    }

    fun getAllCommands(): List<AbstractCmd>  {
        return commandsSet.toList()
    }


    fun searchForCommand(guildData: GuildD, message: Message): CmdSearchData? {
        val msgContent = message.contentRaw.trim()
        val selfID = message.jda.selfUser.id

        if (isBotMentioned(selfID, msgContent)) {
            val cmd = commandsMap["help"]
            return if (cmd != null)
                CmdSearchData(cmd)
            else
                null
        }

        val prefixLength = when {
            msgContent.toLowerCase().startsWith(guildData.prefix) -> guildData.prefix.length
            msgContent.startsWith("<@$selfID>") -> "<@$selfID>".length
            msgContent.startsWith("<@!$selfID>") -> "<@!$selfID>".length
            else -> null
        } ?: return null

        val args = msgContent.drop(prefixLength).trim().split("\\s+".toRegex()).toMutableList()

        val argsTLC = args.map { it.toLowerCase() }.toMutableList()

        val flags = mutableListOf<String>()

        val maxCmdsNesting = Settings.maxCommandsNesting
        val subCmdsCheckLength = if (args.size > maxCmdsNesting) maxCmdsNesting else args.size
        for (i in subCmdsCheckLength downTo 1) {
            val cmdPath = args.take(i).joinToString("/")
            val cmd = getCommandOrNull(cmdPath.toLowerCase())
            if (cmd != null) {
                cmd.flags.forEach {
                    if (argsTLC.contains(it.first.toLowerCase())) {
                        flags.add(it.first.toLowerCase())
                        args.removeAll { arg -> it.first.equals(arg, ignoreCase = true) }
                    }
                }
                return CmdSearchData(cmd, args.drop(cmdPath.split("/").size).toMutableList(), flags)
            }
        }
        return null
    }


    suspend fun runCmd(ctx: CmdCtx) {
        try {
            val guildData = ctx.guildData
            val guild = ctx.guild
            val channel = ctx.channel
            val member = ctx.member
            val cmd = ctx.cmd

            // BOT PERMISSIONS CHECK
            if (!guild.selfMember.hasPermission(ctx.channel, cmd.botChannelPerms.discordPermissions)) {
                try {
                    ctx.respond(Embeds.missingBotChannelPerms(cmd.botChannelPerms))
                } catch (ignored: ErrorResponseException) {}
                return
            }

            // CMD Cooldown check
            val cmdCooldown = CooldownsHandler.getUserCmdCooldown(cmd, ctx.userID)
            if (cmdCooldown != 0L) {
                ctx.respond(Embeds.commandOnCooldown(cmdCooldown, ctx.userID, ctx.cmd.getReadablePath()))
                return
            }

            // DEVELOPER CHECK
            if (cmd.category == CmdCategory.DEVELOPER && !Settings.developerIDs.contains(member.id))
                return

            // USER PERMISSIONS CHECK
            if (cmd.userPerms != CmdUserPerms.NONE && !member.hasPermission(cmd.userPerms.discordPermissions)) {
                ctx.respond(Embeds.missingUserPerms(cmd.userPerms))
                return
            }

            // ARGUMENTS CHECK
            if (cmd.requiresArgs && ctx.args.size == 0) {
                ctx.respond(Embeds.incorrectUsage(guildData.prefix, cmd))
                return
            }

            // HELP DESK SELECTION
            var helpDeskIndex: Int? = 0
            if (cmd.requiresHelpDeskIndex) {
                if (guildData.helpDesks.size == 0) {
                    ctx.respond(
                        Embeds.operationFailed(
                            "This command needs to be applied to a Help Desk but there are 0 Help Desks in this server.",
                            "You can create a new Help Desk with `${guildData.prefix}helpdesk create` or via the `${guildData.prefix}helpdesk` Panel."
                        )
                    )
                    return
                }

                helpDeskIndex = -1

                if (ctx.helpDeskIndex != -1)
                    helpDeskIndex = ctx.helpDeskIndex
                else {
                    val matcher = Pattern.compile("(-\\d+)").matcher(ctx.args.joinToString(" "))
                    if (matcher.find()) {
                        helpDeskIndex = Integer.parseInt(matcher.group(1).substring(1)) - 1
                        if (helpDeskIndex >= ctx.guildData.helpDesks.size)
                            helpDeskIndex = -1
                    }

                    if (helpDeskIndex == -1) {
                        helpDeskIndex = if (guildData.helpDesks.size == 1)
                            0
                        else {
                            ctx.respond(
                                Embeds.helpDeskChoice(
                                    guildData.helpDesks,
                                    guild.selfMember.color ?: Colors.primary
                                )
                            )

                            withTimeoutOrNull(60000) {
                                val event = channel.jda.await<GuildMessageReceivedEvent> {
                                    it.author.id == member.id
                                            && it.channel.id == channel.id
                                            && (it.message.contentRaw == "cancel" ||
                                            (it.message.contentRaw.toIntOrNull() != null
                                                    && it.message.contentRaw.toInt() >= 1
                                                    && it.message.contentRaw.toInt() <= guildData.helpDesks.size))
                                }

                                if (event.message.contentRaw.toLowerCase().startsWith("cancel")) {
                                    event.message.delete().queue()
                                    return@withTimeoutOrNull -1
                                }

                                val index = event.message.contentRaw.toIntOrNull()?.minus(1)
                                event.message.delete().queue()

                                return@withTimeoutOrNull index
                            }
                        }

                        when (helpDeskIndex) {
                            null -> {
                                ctx.respond(
                                    Embeds.operationFailed(
                                        "60 seconds time limit exceed.",
                                        "Next time send the index within 60 seconds."
                                    )
                                )
                                return
                            }
                            -1 -> {
                                ctx.respond(Embeds.operationCanceled("Canceled via `cancel`."))
                                return
                            }
                        }
                    }
                }
            }

            ctx.helpDeskIndex = helpDeskIndex!!


            // UNIQUE USAGE CHECK
            if (cmd.uniqueUsage) {
                if (!UsageCache.executedCommand(guild.id, cmd)) {
                    ctx.respond(Embeds.commandAlreadyInUse)
                    return
                }
            }


            // WRITE COMMAND USAGE IN INFLUXDB
            GuildStatsController.writeCommand(cmd.getDefaultPath(), ctx.userID, ctx.guildID)

            // RUN THE COMMAND
            cmd.run(ctx)


            // ONCE TERMINATED, REMOVE IT FROM THE UNIQUE USAGE CACHE
            if (cmd.uniqueUsage)
                UsageCache.terminatedCommand(guild.id, cmd)

        } catch (e: InsufficientPermissionException) {
            if (ctx.cmd.uniqueUsage)
                UsageCache.terminatedCommand(ctx.guildID, ctx.cmd)

            if (ctx.guild.selfMember.hasPermission(ctx.channel, BotChannelPerms.MESSAGES.discordPermissions)) {
                ctx.respond(Embeds.missingBotChannelPerms(ctx.cmd.botChannelPerms))
            } else {
                try {
                    ctx.member.user.openPrivateChannel()
                        .flatMap {
                            it.sendMessage(Embeds.missingBotChannelPerms(ctx.cmd.botChannelPerms))
                        }
                        .queue()
                } catch (ignored: Exception) {}
            }

            if (Settings.logPermissionExceptions)
                logger.error("Permission issue in the Commands Handler", e)
        } catch (e: Exception) {
            if (ctx.cmd.uniqueUsage)
                UsageCache.terminatedCommand(ctx.guildID, ctx.cmd)

            if (e !is UnsupportedOperationException) {
                logger.error("Caught exception while running a command", e)
                WebhooksService.sendErrorWebhook(e)
            }

            ctx.respond(Embeds.unknownFailure)
        }
    }


    private fun isBotMentioned(botID: String, string: String): Boolean {
        return string == "<@$botID>" || string == "<@!$botID>"
    }
}
