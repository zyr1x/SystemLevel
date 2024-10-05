package ru.lewis.systemlevel.service

import dev.rollczi.litecommands.adventure.LiteAdventureExtension
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory
import dev.rollczi.litecommands.bukkit.LiteBukkitMessages
import jakarta.inject.Inject
import jakarta.inject.Singleton
import me.lucko.helper.terminable.TerminableConsumer
import me.lucko.helper.terminable.module.TerminableModule
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import org.bukkit.plugin.Plugin
import ru.lewis.systemlevel.commands.PlayerLevelArgument
import ru.lewis.systemlevel.commands.PlayerLevelContext
import ru.lewis.systemlevel.commands.admin.SystemLevelCommand
import ru.lewis.systemlevel.commands.default.LevelCommand
import ru.lewis.systemlevel.model.level.PlayerLevel

@Singleton
class CommandService @Inject constructor(
    private val plugin: Plugin,
    private val configurationService: ConfigurationService,
    private val playerLevelArgument: PlayerLevelArgument,
    private val playerLevelContext: PlayerLevelContext,
    private val systemLevelCommand: SystemLevelCommand,
    private val levelCommand: LevelCommand
) : TerminableModule {

    @Suppress("UnstableApiUsage")
    override fun setup(consumer: TerminableConsumer) {
        LiteBukkitFactory.builder(plugin.name, plugin)

            // commands
            .commands(systemLevelCommand, levelCommand)

            // context
            .context(PlayerLevel::class.java, playerLevelContext)

            // arguments
            .argument(PlayerLevel::class.java, playerLevelArgument)

            .message(LiteBukkitMessages.INVALID_USAGE, configurationService.messages.help.asComponent())

            // extensions
            .extension(LiteAdventureExtension()) { config ->
                config.miniMessage(true)
                config.legacyColor(true)
                config.colorizeArgument(true)
                config.serializer(miniMessage())
            }
                .build()
    }
}