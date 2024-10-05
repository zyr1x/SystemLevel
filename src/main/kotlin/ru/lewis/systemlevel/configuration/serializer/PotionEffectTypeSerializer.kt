package ru.lewis.systemlevel.configuration.serializer

import jakarta.inject.Inject
import org.bukkit.potion.PotionEffectType
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

class PotionEffectTypeSerializer @Inject constructor() : TypeSerializer<PotionEffectType> {

    override fun deserialize(type: Type, node: ConfigurationNode): PotionEffectType? {
        return node.string?.let {
            PotionEffectType.getByName(it) ?: throw IllegalArgumentException("Unknown potion effect type $it")
        }

    }

    override fun serialize(type: Type, obj: PotionEffectType?, node: ConfigurationNode) {
        node.set(obj?.name)
    }
}
