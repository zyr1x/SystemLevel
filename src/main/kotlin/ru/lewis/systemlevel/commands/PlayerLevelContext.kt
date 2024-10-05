package ru.lewis.systemlevel.commands

import dev.rollczi.litecommands.context.ContextProvider
import dev.rollczi.litecommands.context.ContextResult
import dev.rollczi.litecommands.invocation.Invocation
import jakarta.inject.Inject
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import ru.lewis.systemlevel.model.level.PlayerLevel
import ru.lewis.systemlevel.service.ConfigurationService
import ru.lewis.systemlevel.service.LevelService

class PlayerLevelContext @Inject constructor(

    private val levelService: LevelService,
    private val configurationService: ConfigurationService

) : ContextProvider<CommandSender, PlayerLevel> {

    private val errors get() = configurationService.messages.error

    override fun provide(invocation: Invocation<CommandSender>?): ContextResult<PlayerLevel> {

        if (invocation!!.sender() is Player) {

            val player = invocation.sender() as Player
            return ContextResult.ok { levelService.getPlayer(player) }

        }

        return ContextResult.error(errors.playerNotFound.asComponent())
    }
}