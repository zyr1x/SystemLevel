package ru.lewis.systemlevel.configuration.serializer

import org.bukkit.enchantments.Enchantment
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

class EnchantmentSerializer : TypeSerializer<Enchantment> {

    @Suppress("DEPRECATION")
    override fun deserialize(type: Type, node: ConfigurationNode): Enchantment? {
        return node.string?.let { Enchantment.getByName(it) }
    }

    @Suppress("DEPRECATION")
    override fun serialize(type: Type, obj: Enchantment?, node: ConfigurationNode) {
        node.set(obj?.let { it.name })
    }
}
