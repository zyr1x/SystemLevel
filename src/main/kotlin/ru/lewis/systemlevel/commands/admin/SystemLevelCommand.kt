package ru.lewis.systemlevel.commands.admin

import dev.rollczi.litecommands.annotations.argument.Arg
import dev.rollczi.litecommands.annotations.command.Command
import dev.rollczi.litecommands.annotations.context.Context
import dev.rollczi.litecommands.annotations.execute.Execute
import dev.rollczi.litecommands.annotations.permission.Permission
import jakarta.inject.Inject
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import ru.lewis.systemlevel.model.level.PlayerLevel
import ru.lewis.systemlevel.service.ConfigurationService

@Command(name = "systemlevel")
@Permission("systemlevel.admin")
class SystemLevelCommand @Inject constructor(
    private val configurationService: ConfigurationService
){

    private val messages get() = configurationService.messages

    @Execute(name = "reload")
    fun reload(@Context sender: PlayerLevel) {
        sender.player.sendMessage(messages.reload)
        configurationService.reload()
    }

    @Execute(name = "update exp me")
    fun updateExpMe(@Context sender: PlayerLevel, @Arg exp: Double) {

        sender.player.sendMessage(
            messages.exp.adminInfo.feedBack.resolve(
                Placeholder.unparsed(
                    "lastexp",
                    sender.currentExp.toString()
                ),
                Placeholder.unparsed(
                    "newexp",
                    (sender.currentExp + exp).toString()
                )
            )
        )

        sender.updateExp(exp)
    }

    @Execute(name = "update exp")
    fun updateExpOther(@Context sender: PlayerLevel, @Arg target: PlayerLevel, @Arg exp: Double) {

        sender.player.sendMessage(
            messages.exp.adminInfo.feedBack.resolve(
                Placeholder.unparsed(
                    "lastexp",
                    target.currentExp.toString()
                ),
                Placeholder.unparsed(
                    "newexp",
                    (sender.currentExp + exp).toString()
                )
            )
        )

        target.updateExp(exp)
    }

    @Execute(name = "set exp me")
    fun setExpMe(@Context sender: PlayerLevel, @Arg exp: Double) {

        sender.player.sendMessage(
            messages.exp.adminInfo.feedBack.resolve(
                Placeholder.unparsed(
                    "lastexp",
                    sender.currentExp.toString()
                ),
                Placeholder.unparsed(
                    "newexp",
                    exp.toString()
                )
            )
        )

        sender.setExp(exp)
    }

    @Execute(name = "set exp")
    fun setExpOther(@Context sender: PlayerLevel, @Arg target: PlayerLevel, @Arg exp: Double) {

        sender.player.sendMessage(
            messages.exp.adminInfo.feedBack.resolve(
                Placeholder.unparsed(
                    "lastexp",
                    target.currentExp.toString()
                ),
                Placeholder.unparsed(
                    "newexp",
                    exp.toString()
                )
            )
        )

        target.setExp(exp)
    }

    @Execute(name = "set level me")
    fun setLevelMe(@Context sender: PlayerLevel, @Arg level: Int) {

        sender.player.sendMessage(
            messages.level.adminInfo.feedBack.resolve(
                Placeholder.unparsed(
                    "lastexp",
                    sender.currentExp.toString()
                ),
                Placeholder.unparsed(
                    "newexp",
                    level.toString()
                )
            )
        )

        sender.setLevel(level)
    }

    @Execute(name = "set exp")
    fun setLevelOther(@Context sender: PlayerLevel, @Arg target: PlayerLevel, @Arg level: Int) {

        sender.player.sendMessage(
            messages.level.adminInfo.feedBack.resolve(
                Placeholder.unparsed(
                    "lastexp",
                    target.currentExp.toString()
                ),
                Placeholder.unparsed(
                    "neweaxp",
                    level.toString()
                )
            )
        )

        target.setLevel(level)
    }

    @Execute(name = "reset me")
    fun resetMe(@Context sender: PlayerLevel) {

        sender.player.sendMessage(
            messages.reset.info.feedBack.resolve(
                Placeholder.unparsed(
                    "player",
                    sender.player.name
                )
            )
        )

        sender.reset()
    }

    @Execute(name = "reset")
    fun resetOther(@Context sender: PlayerLevel, @Arg target: PlayerLevel) {

        sender.player.sendMessage(
            messages.reset.info.feedBack.resolve(
                Placeholder.unparsed(
                    "player",
                    target.player.name
                )
            )
        )

        target.player.sendMessage(
            messages.reset.info.feedBackTarget.resolve(
                Placeholder.unparsed(
                    "player",
                    sender.player.name
                )
            )
        )

        target.reset()
    }

    @Execute(name = "save me")
    fun saveMe(@Context sender: PlayerLevel) {

        sender.player.sendMessage(
            messages.save.info.feedBack.resolve(
                Placeholder.unparsed(
                    "player",
                    sender.player.name
                )
            )
        )

        sender.save()
    }

    @Execute(name = "save")
    fun saveOther(@Context sender: PlayerLevel, @Arg target: PlayerLevel) {

        sender.player.sendMessage(
            messages.save.info.feedBack.resolve(
                Placeholder.unparsed(
                    "player",
                    target.player.name
                )
            )
        )

        target.player.sendMessage(
            messages.save.info.feedBackTarget.resolve(
                Placeholder.unparsed(
                    "player",
                    sender.player.name
                )
            )
        )

        target.save()
    }

}