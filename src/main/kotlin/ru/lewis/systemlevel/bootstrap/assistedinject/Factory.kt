package ru.lewis.systemlevel.bootstrap.assistedinject

import org.bukkit.entity.Player
import ru.lewis.systemlevel.configuration.type.MiniMessageComponent
import ru.lewis.systemlevel.model.level.PlayerLevel

interface Factory {

    fun create(
        player: Player,
        id: Int,
        displayName: MiniMessageComponent,
        currentExp: Double
    ): PlayerLevel

}