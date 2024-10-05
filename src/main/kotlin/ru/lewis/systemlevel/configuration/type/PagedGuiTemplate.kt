package ru.lewis.systemlevel.configuration.type

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item

@ConfigSerializable
data class PagedGuiTemplate(
    val title: MiniMessageComponent? = null,
    val structure: Array<String>? = null,
    val contentsSettings: ContentsSettings? = null,
    val templates: MutableMap<Char, ItemTemplate>? = null,
    val customItems: MutableMap<Char, ItemTemplate>? = null,
) {

    private val items: MutableMap<Char, Item> = mutableMapOf()
    private val contents: MutableList<Item> = mutableListOf()

    fun addItem(symbol: Char, item: Item): PagedGuiTemplate {
        items[symbol] = item

        return this
    }

    fun addContents(items: List<Item>): PagedGuiTemplate {
        contents.addAll(items)

        return this
    }

    fun toGui(): Gui {

        val guiBuilder = PagedGui.items()

        if (structure != null) {
            guiBuilder.setStructure(*structure)
        }

        if (contentsSettings != null) {
            guiBuilder.addIngredient(contentsSettings.symbol, if (contentsSettings.isHorizontal) Markers.CONTENT_LIST_SLOT_HORIZONTAL else Markers.CONTENT_LIST_SLOT_VERTICAL)
        }

        if (templates != null) {

            for (item in templates) {
                guiBuilder.addIngredient(item.key, item.value.toItem())
            }

        }

        if (customItems != null) {

            for (item in customItems) {
                guiBuilder.addIngredient(item.key, item.value.toItem())
            }

        }

        for (item in items) {
            guiBuilder.addIngredient(item.key, item.value)
        }

        guiBuilder.setContent(contents)

        return guiBuilder.build()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PagedGuiTemplate

        if (title != other.title) return false
        if (structure != null) {
            if (other.structure == null) return false
            if (!structure.contentEquals(other.structure)) return false
        } else if (other.structure != null) return false
        if (contentsSettings != other.contentsSettings) return false
        if (templates != other.templates) return false
        if (customItems != other.customItems) return false
        if (items != other.items) return false
        if (contents != other.contents) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title?.hashCode() ?: 0
        result = 31 * result + (structure?.contentHashCode() ?: 0)
        result = 31 * result + (contentsSettings?.hashCode() ?: 0)
        result = 31 * result + (templates?.hashCode() ?: 0)
        result = 31 * result + (customItems?.hashCode() ?: 0)
        result = 31 * result + items.hashCode()
        result = 31 * result + contents.hashCode()
        return result
    }

    @ConfigSerializable
    data class ContentsSettings(
        val isHorizontal: Boolean,
        val symbol: Char
    )
}