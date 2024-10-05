package ru.lewis.systemlevel.configuration

import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import ru.lewis.systemlevel.configuration.type.ItemTemplate
import ru.lewis.systemlevel.configuration.type.LevelConfiguration
import ru.lewis.systemlevel.configuration.type.MiniMessageComponent
import ru.lewis.systemlevel.extension.asMiniMessageComponent

@ConfigSerializable
data class LevelsConfiguration(

    val levelsSettings: Levels = Levels(),
    val experienceUpdateQuest: ExperienceUpdateQuest = ExperienceUpdateQuest()

) {

    @ConfigSerializable
    data class Levels(

        val defaultLevel: LevelConfiguration = LevelConfiguration(
            id = 1,
            displayName = "<gray>Новичок".asMiniMessageComponent()
        ),

        val levels: List<LevelConfiguration> = listOf(
            LevelConfiguration(
                2,
                "<white>Пельмень".asMiniMessageComponent(),
                ItemMenu(),
                100.0,
                100,
                listOf("gm 0 <player>")
            ),
            LevelConfiguration(
                3,
                "<yellow>Волк".asMiniMessageComponent(),
                ItemMenu(),
                200.0,
                100,
                listOf("gm 1 <player>")
            )
        )

    ) {

        @ConfigSerializable
        data class ItemMenu(

            val itemIsClosed: ItemTemplate = ItemTemplate(Material.BARRIER),
            val itemIsProcess: ItemTemplate = ItemTemplate(Material.YELLOW_WOOL),
            val itemIsComplete: ItemTemplate = ItemTemplate(Material.LIME_WOOL)

        )
    }

    @ConfigSerializable
    data class ExperienceUpdateQuest(

        val entity: Entity = Entity(),
        val blocks: Blocks = Blocks()

    ) {

        @ConfigSerializable
        data class Entity(

            val default: Exp? = Exp(0.1, 0.5),

            val other: MutableMap<EntityType, Exp> = mutableMapOf(
                Pair(
                    EntityType.BEE,
                    Exp(
                        0.5,
                        1.0
                    )
                )
            )

        )

        @ConfigSerializable
        data class Blocks(

            val default: Exp? = Exp(0.1, 0.5),

            val other: MutableMap<Material, Exp> = mutableMapOf(
                Pair(
                    Material.STONE,
                    Exp(
                        0.5,
                        1.0
                    )
                )
            )

        )

        @ConfigSerializable
        data class Exp(
            val min: Double,
            val max: Double
        )

    }

}
