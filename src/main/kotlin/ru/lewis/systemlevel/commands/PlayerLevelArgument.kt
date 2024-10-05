package ru.lewis.systemlevel.commands

import dev.rollczi.litecommands.argument.Argument
import dev.rollczi.litecommands.argument.parser.ParseResult
import dev.rollczi.litecommands.argument.resolver.ArgumentResolver
import dev.rollczi.litecommands.invocation.Invocation
import dev.rollczi.litecommands.suggestion.SuggestionContext
import dev.rollczi.litecommands.suggestion.SuggestionResult
import jakarta.inject.Inject
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import ru.lewis.systemlevel.model.level.PlayerLevel
import ru.lewis.systemlevel.service.ConfigurationService
import ru.lewis.systemlevel.service.LevelService

class PlayerLevelArgument @Inject constructor(

    private val levelService: LevelService,
    private val configurationService: ConfigurationService

) : ArgumentResolver<CommandSender, PlayerLevel>() {

    private val errors get() = configurationService.messages.error

    override fun parse(
        invocation: Invocation<CommandSender>?,
        p1: Argument<PlayerLevel>?,
        argument: String?
    ): ParseResult<PlayerLevel> {

        val player = Bukkit.getPlayer(argument!!) ?: return ParseResult.failure(errors.playerNotFound.asComponent())
        return ParseResult.success(levelService.getPlayer(player))

    }

    override fun suggest(
        invocation: Invocation<CommandSender>?,
        argument: Argument<PlayerLevel>?,
        context: SuggestionContext?
    ): SuggestionResult {
        return SuggestionResult.of( Bukkit.getOnlinePlayers().map { it.name } )
    }


}