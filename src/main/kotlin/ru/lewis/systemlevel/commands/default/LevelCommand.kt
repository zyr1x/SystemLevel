package ru.lewis.systemlevel.commands.default

import dev.rollczi.litecommands.annotations.command.Command
import dev.rollczi.litecommands.annotations.context.Context
import dev.rollczi.litecommands.annotations.execute.Execute
import jakarta.inject.Inject
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import ru.lewis.systemlevel.configuration.type.ItemTemplate
import ru.lewis.systemlevel.configuration.type.MiniMessageComponent
import ru.lewis.systemlevel.configuration.type.import
import ru.lewis.systemlevel.model.level.PlayerLevel
import ru.lewis.systemlevel.model.menu.buttons.BackButton
import ru.lewis.systemlevel.model.menu.buttons.ForwardButton
import ru.lewis.systemlevel.service.ConfigurationService
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.builder.ItemBuilder
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.window.type.context.setTitle

@Command(name = "level", aliases = ["levels"])
class LevelCommand @Inject constructor(
    private val configurationService: ConfigurationService
) {

    private val levelsConfiguration get() = configurationService.levels.levelsSettings
    private val guiConfiguration get() = configurationService.menu
    private val buttons get() = guiConfiguration.buttons

    @Execute
    fun execute(@Context sender: PlayerLevel) {

        this.openMenu(sender)

    }

    private fun openMenu(player: PlayerLevel) {

        Window.single().apply {

            val gui = guiConfiguration.gui

            import(gui) {
                addIngredient(buttons.back.symbol, BackButton(buttons.back.item))
                addIngredient(buttons.forward.symbol, ForwardButton(buttons.forward.item))
                setContent(levelsConfiguration.levels.map { level ->
                    LevelButton(
                        level.id,
                        level.displayName,
                        level.itemInMenu?.itemIsClosed ?: throw NullPointerException("настройте, пожалуйста, предметы в конфигурации для Левелов."),
                        level.itemInMenu.itemIsProcess,
                        level.itemInMenu.itemIsComplete,
                        player,
                        level.price ?: 0
                    )
                })
            }

            setTitle(gui.title.asComponent())

        }.open(player.player)

    }

    private inner class LevelButton @Inject constructor(

        private val levelId: Int,
        private val displayName: MiniMessageComponent,

        private val itemIsClosed: ItemTemplate,
        private val itemIsProcess: ItemTemplate,
        private val itemIsComplete: ItemTemplate,

        private val playerLevel: PlayerLevel,

        private val price: Int

    ): AbstractItem() {

        private var isCurrentLevel = false

        override fun getItemProvider(): ItemProvider {

            return if (playerLevel.id >= levelId) {
                ItemBuilder(
                    itemIsComplete.resolve(
                        Placeholder.unparsed(
                            "price",
                            this.price.toString()
                        ),
                        Placeholder.unparsed(
                            "currentexp",
                            playerLevel.currentExp.toString()
                        ),
                        Placeholder.unparsed(
                            "currentlevel",
                            playerLevel.id.toString()
                        ),
                        Placeholder.unparsed(
                            "thislevel",
                            levelId.toString()
                        ),
                        Placeholder.component(
                            "displayname",
                            displayName
                        )
                    ).toItem()
                )
            } else if (playerLevel.getNextLevel().id == levelId) {
                isCurrentLevel = true
                ItemBuilder(
                    itemIsProcess.resolve(
                        Placeholder.unparsed(
                            "price",
                            this.price.toString()
                        ),
                        Placeholder.unparsed(
                            "currentexp",
                            playerLevel.currentExp.toString()
                        ),
                        Placeholder.unparsed(
                            "currentlevel",
                            playerLevel.id.toString()
                        ),
                        Placeholder.unparsed(
                            "thislevel",
                            levelId.toString()
                        ),
                        Placeholder.component(
                            "displayname",
                            displayName
                        )
                    ).toItem()
                )
            } else {
                ItemBuilder(
                    itemIsClosed.resolve(
                        Placeholder.unparsed(
                            "price",
                            this.price.toString()
                        ),
                        Placeholder.unparsed(
                            "currentexp",
                            playerLevel.currentExp.toString()
                        ),
                        Placeholder.unparsed(
                            "currentlevel",
                            playerLevel.id.toString()
                        ),
                        Placeholder.unparsed(
                            "thislevel",
                            levelId.toString()
                        ),
                        Placeholder.component(
                            "displayname",
                            displayName
                        )
                    ).toItem()
                )
            }
        }

        override fun handleClick(p0: ClickType, player: Player, event: InventoryClickEvent) {

            if (isCurrentLevel) {

                if (playerLevel.buyNextLevel()) {
                    this.isCurrentLevel = false
                    openMenu(playerLevel)
                } else {
                    event.inventory.close()
                }

            }
        }


    }

}