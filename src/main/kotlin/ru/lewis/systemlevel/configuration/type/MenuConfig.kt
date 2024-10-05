package ru.lewis.systemlevel.configuration.type

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import ru.lewis.systemlevel.extension.asMiniMessageComponent
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.window.type.context.setTitle
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@ConfigSerializable
open class MenuConfig(
    val title: MiniMessageComponent = "".asMiniMessageComponent(),
    val structure: List<String> = listOf(),
    val customItems: Map<Char, ItemTemplate> = mapOf(),
    val templates: Map<Char, ItemTemplate> = mapOf()
)

@OptIn(ExperimentalContracts::class)
inline fun Window.Builder.Normal.Single.import(
    config: MenuConfig,
    guiModifier: PagedGui.Builder<Item>.() -> Unit
): PagedGui<Item> {
    contract {
        callsInPlace(guiModifier, InvocationKind.EXACTLY_ONCE)
    }

    val gui = config.createPagedGui().apply(guiModifier).build()
    import(config, gui)
    return gui
}

fun Window.Builder.Normal.Single.import(config: MenuConfig, gui: Gui) {
    setTitle(config.title.asComponent())
    setGui(gui)
}

fun MenuConfig.createPagedGui(): PagedGui.Builder<Item> = PagedGui.items().apply {
    setStructure(*this@createPagedGui.structure.toTypedArray())
    addIngredient('.', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
    this@createPagedGui.customItems.forEach { (key, item) -> addIngredient(key, ItemWrapper(item.toItem())) }
}
