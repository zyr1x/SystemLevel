package ru.lewis.systemlevel.configuration.type

import de.tr7zw.changeme.nbtapi.NBT
import de.tr7zw.changeme.nbtapi.NBTCompoundList
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionType
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import ru.lewis.systemlevel.extension.asMiniMessageComponent
import ru.lewis.systemlevel.extension.setSkullTexture
import ru.lewis.systemlevel.extension.toAdventure
import ru.lewis.systemlevel.extension.toAwtColor
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder
import xyz.xenondevs.invui.item.builder.ItemBuilder
import xyz.xenondevs.invui.item.builder.PotionBuilder
import java.awt.Color

@ConfigSerializable
data class ItemTemplate(
    val type: Material,
    val amount: Int? = null,
    val displayName: MiniMessageComponent? = null,
    val lore: List<MiniMessageComponent>? = null,
    val flags: Set<ItemFlag>? = null,
    val enchantments: Map<Enchantment, Int>? = null,
    val unbreakable: Boolean? = null,
    val potion: PotionTemplate? = null,
    val skullTexture: String? = null,
    val storedEnchantments: Map<Enchantment, Int>? = null,
    val attributes: List<AttributeConfiguration>? = null,
    val bukkitPublicValues: List<String>? = null
) {

    @ConfigSerializable
    data class PotionTemplate(
        val type: PotionType,
        val extended: Boolean,
        val upgraded: Boolean,
        val color: Color?,
        val effects: List<PotionEffect>?
    )

    fun resolve(vararg tagResolvers: TagResolver): ItemTemplate {
        return copy(
            displayName = displayName?.resolve(*tagResolvers),
            lore = lore?.map { line ->
                (line as MiniMessageComponent?)?.resolve(*tagResolvers) ?: Component.empty().asMiniMessageComponent()
            }
        )
    }

    fun toItem(): ItemStack {
        val builder = createBuilder()

        builder.amount = amount ?: 1

        displayName?.also { builder.setDisplayName(AdventureComponentWrapper(it.asComponent())) }

        if (lore != null) {
            builder.setLore(lore.map { AdventureComponentWrapper(it.asComponent()) })
        }

        if (flags != null) {
            builder.setItemFlags(flags.toMutableList())
        }

        enchantments?.forEach { (enchantment, level) ->
            builder.addEnchantment(enchantment, level, true)
        }

        builder.setUnbreakable(unbreakable ?: false)

        if (potion != null && builder is PotionBuilder) {

            potion.color?.let { builder.setColor(it) }

            if (potion.effects != null) {
                potion.effects.forEach { builder.addEffect(it) }
            }

            builder.setBasePotionData(
                PotionData(
                    potion.type,
                    potion.extended,
                    potion.upgraded
                )
            )
        }

        val item = builder.get()

        if (skullTexture != null) {
            item.setSkullTexture(skullTexture)
        }

        if (storedEnchantments != null) {
            (item.itemMeta as? EnchantmentStorageMeta)?.let {
                storedEnchantments.forEach { (enchantment, level) -> it.addStoredEnchant(enchantment, level, true) }
                item.setItemMeta(it)
            }
        }

        if (attributes != null) {
            NBT.modify(item) { nbt ->
                nbt.modifyMeta { _, meta ->
                    attributes.forEach {
                        meta.addAttributeModifier(it.attribute, it.modifier)
                    }
                }
            }
        }

        if (bukkitPublicValues != null) {
            NBT.modify(item) { nbt ->
                val list = nbt.getCompoundList("PublicBukkitValues") as NBTCompoundList
                bukkitPublicValues.asSequence()
                    .map { NBT.parseNBT(it) }
                    .forEachIndexed { index, compound -> list.add(index, compound) }
            }
        }

        return item
    }

    private fun createBuilder(): AbstractItemBuilder<*> {
        return when (type) {
            Material.POTION -> PotionBuilder(PotionBuilder.PotionType.NORMAL)
            Material.SPLASH_POTION -> PotionBuilder(PotionBuilder.PotionType.SPLASH)
            Material.LINGERING_POTION -> PotionBuilder(PotionBuilder.PotionType.LINGERING)
            Material.TIPPED_ARROW -> PotionBuilder(ItemStack(Material.TIPPED_ARROW))
            else -> ItemBuilder(type)
        }
    }

    companion object {

        @OptIn(ExperimentalStdlibApi::class)
        @Suppress("DEPRECATION")
        fun fromItem(item: ItemStack): ItemTemplate {
            val displayName: MiniMessageComponent?
            val lore: List<MiniMessageComponent>?
            val flags: Set<ItemFlag>?
            val enchantments: Map<Enchantment, Int>?
            val unbreakable: Boolean?
            val potionTemplate: PotionTemplate?
            var skullTexture: String? = null
            val storedEnchantments: Map<Enchantment, Int>?
            val attributes: MutableList<AttributeConfiguration>?
            var publicBukkitValues: List<String>? = null

            with(item.itemMeta) {
                displayName = if (this.hasDisplayName()) {
                    this.displayNameComponent.toAdventure().asMiniMessageComponent()
                } else {
                    null
                }

                lore = this.loreComponents?.map { it.toAdventure().asMiniMessageComponent() }

                flags = this.itemFlags.takeIf { it.isNotEmpty() }

                enchantments = this.enchants.takeIf { it.isNotEmpty() }

                unbreakable = this.isUnbreakable.takeIf { it }

                potionTemplate = if (this is PotionMeta) {
                    PotionTemplate(
                        this.basePotionData.type,
                        this.basePotionData.isExtended,
                        this.basePotionData.isUpgraded,
                        this.color?.toAwtColor(),
                        if (this.hasCustomEffects()) this.customEffects else null
                    )
                } else null

                storedEnchantments = if (this is EnchantmentStorageMeta) this.storedEnchants else null

                if (item.type == Material.PLAYER_HEAD) {
                    NBT.get(item) {
                        skullTexture = it.getCompound("SkullOwner")
                            ?.getCompound("Properties")
                            ?.getCompoundList("textures")
                            ?.find { nbt -> nbt.hasTag("Value") }
                            ?.getString("Value")
                            ?.takeIf { texture -> texture.isNotBlank() }
                    }
                }

                if (!this.hasAttributeModifiers()) {
                    attributes = null
                } else {
                    attributes = mutableListOf()
                    Attribute.entries.forEach { attribute ->
                        this.getAttributeModifiers(attribute)?.forEach { modifier ->
                            attributes += AttributeConfiguration(attribute, modifier)
                        }
                    }
                }

                NBT.get(item) { nbt ->
                    publicBukkitValues = if (!nbt.hasTag("PublicBukkitValues")) null
                    else nbt.getCompoundList("PublicBukkitValues")?.map { it.toString() }
                }
            }

            return ItemTemplate(
                item.type,
                item.amount.takeIf { it != 1 },
                displayName,
                lore,
                flags,
                enchantments,
                unbreakable,
                potionTemplate,
                skullTexture,
                storedEnchantments,
                attributes,
                publicBukkitValues
            )
        }
    }
}

