package ru.lewis.systemlevel.configuration

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import ru.lewis.systemlevel.configuration.type.MiniMessageComponent
import ru.lewis.systemlevel.extension.asMiniMessageComponent

@ConfigSerializable
data class MessagesConfiguration(

    val level: LevelMessages = LevelMessages(),
    val exp: ExpMessages = ExpMessages(),
    val error: ErrorMessages = ErrorMessages(),
    val save: SaveMessages = SaveMessages(),
    val reset: ResetMessages = ResetMessages(),

    val help: MiniMessageComponent = "".asMiniMessageComponent(),
    val reload: MiniMessageComponent = "<green>Вы успешно перезагрузили конфигурацию плагина!".asMiniMessageComponent()
) {

    @ConfigSerializable
    data class SaveMessages(
        val info: InfoMessages = InfoMessages()
    ) {

        @ConfigSerializable
        data class InfoMessages(
            val feedBack: MiniMessageComponent = "<green>Вы успешно сохранили в базу данных игрока <player>".asMiniMessageComponent(),
            val feedBackTarget: MiniMessageComponent = "<green>Вас насильно сохранили в базу данных.".asMiniMessageComponent()
        )
    }

    @ConfigSerializable
    data class ResetMessages(
        val info: InfoMessages = InfoMessages()
    ) {

        @ConfigSerializable
        data class InfoMessages(
            val feedBack: MiniMessageComponent = "<red>Вы сбросили игрока <player>.".asMiniMessageComponent(),
            val feedBackTarget: MiniMessageComponent = "<red>Адмнистратор <player> решил сбросить ваш уровень на заводские найстройки.".asMiniMessageComponent()
        )

    }

    @ConfigSerializable
    data class LevelMessages(
        val info: InfoMessages = InfoMessages(),
        val adminInfo: AdminInfoMessages = AdminInfoMessages()
    ) {
        @ConfigSerializable
        data class InfoMessages(
            val showMessage: Boolean = true,
            val feedBack: MiniMessageComponent = "<green>Ваш уровень обновлен с <lastLevel> до <newLevel>!".asMiniMessageComponent()
        )

        @ConfigSerializable
        data class AdminInfoMessages(
            val feedBack: MiniMessageComponent = "<green>Вы успешно обновили уровень игрока с <lastLevel> до <newLevel>".asMiniMessageComponent()
        )
    }

    @ConfigSerializable
    data class ExpMessages(

        val info: InfoMessages = InfoMessages(),
        val adminInfo: AdminInfoMessages = AdminInfoMessages()

    ) {

        @ConfigSerializable
        data class InfoMessages(
            val showMessage: Boolean = false,
            val feedBack: MiniMessageComponent = "<green>Ваш опыт обновлен с <lastExp> до <newExp>!".asMiniMessageComponent()
        )

        @ConfigSerializable
        data class AdminInfoMessages(
            val feedBack: MiniMessageComponent = "<green>Вы успешно обновили опыт игрока с <lastExp> до <newExp>".asMiniMessageComponent()
        )
    }

    @ConfigSerializable
    data class ErrorMessages(

        val playerNotFound: MiniMessageComponent = "Произошла внутреняя ошибка, из-за которой небыл найден игрок, обратитесь к администратору.".asMiniMessageComponent()
    )

}