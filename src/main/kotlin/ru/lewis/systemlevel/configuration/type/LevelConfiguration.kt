package ru.lewis.systemlevel.configuration.type

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import ru.lewis.systemlevel.configuration.LevelsConfiguration.Levels.ItemMenu

@ConfigSerializable
data class LevelConfiguration(
    val id: Int,
    val displayName: MiniMessageComponent,
    val itemInMenu: ItemMenu? = null,
    val upExp: Double? = null,
    val price: Int? = null,
    val commands: List<String>? = null,
)