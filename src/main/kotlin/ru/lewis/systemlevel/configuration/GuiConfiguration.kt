package ru.lewis.systemlevel.configuration

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import ru.lewis.systemlevel.configuration.type.ItemTemplate
import ru.lewis.systemlevel.configuration.type.MenuConfig
import ru.lewis.systemlevel.extension.asMiniMessageComponent

@ConfigSerializable
data class GuiConfiguration(

    val gui: MenuConfig = MenuConfig(

        title = "".asMiniMessageComponent(),
        structure = listOf(
            "# # # # # # # # #",
            "# . . . . . . . #",
            "# . . . . . . . #",
            "I # # < # > # # #",
        ),
        customItems = mutableMapOf(
            Pair(
                '#',
                ItemTemplate(
                    Material.RED_STAINED_GLASS_PANE
                )
            ),
            Pair(
                'I',
                ItemTemplate(
                    Material.PAPER
                )
            )

        )
    ),

    val buttons: Buttons = Buttons()

) {

    @ConfigSerializable
    data class Buttons(

        val back: BackButton = BackButton(),
        val forward: ForwardButton = ForwardButton()

    ) {
        @ConfigSerializable
        data class BackButton(
            val symbol: Char = '<',
            val item: ItemTemplate = ItemTemplate(Material.ARROW, enchantments = mutableMapOf(Pair(Enchantment.LUCK, 1)))
        )

        @ConfigSerializable
        data class ForwardButton(
            val symbol: Char = '>',
            val item: ItemTemplate = ItemTemplate(Material.SPECTRAL_ARROW)
        )
    }

}