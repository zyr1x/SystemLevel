package ru.lewis.systemlevel.model.menu.buttons

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import ru.lewis.systemlevel.configuration.type.ItemTemplate
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.builder.ItemBuilder
import xyz.xenondevs.invui.item.impl.controlitem.PageItem

class ForwardButton(
    private val item: ItemTemplate
): PageItem(true) {

    override fun getItemProvider(p0: PagedGui<*>?): ItemProvider {
        return ItemBuilder(
            item.resolve(
                Placeholder.unparsed(
                    "currentpage",
                    gui.currentPage.toString()
                ),
                Placeholder.unparsed(
                    "pageamount",
                    gui.pageAmount.toString()
                )
            ).toItem()
        )
    }
}