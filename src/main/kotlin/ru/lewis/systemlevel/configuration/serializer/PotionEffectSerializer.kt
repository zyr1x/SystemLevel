package ru.lewis.systemlevel.configuration.serializer

import jakarta.inject.Inject
import net.kyori.adventure.util.Ticks
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.serialize.TypeSerializer
import ru.lewis.systemlevel.extension.inTicks
import java.lang.reflect.Type
import java.time.Duration
import kotlin.time.toKotlinDuration

class PotionEffectSerializer @Inject constructor() : TypeSerializer<PotionEffect> {

    override fun deserialize(type: Type, node: ConfigurationNode): PotionEffect? {
        val duration = node.node("duration").get(Duration::class) ?: return null
        val amplifier = node.node("amplifier").getInt(1)
        val ambient = node.node("ambient").getBoolean(true)
        val particles = node.node("particles").getBoolean(true)
        val icon = node.node("icon").getBoolean(true)

        return PotionEffect(
            node.node("type").get(PotionEffectType::class)!!,
            duration.toKotlinDuration().inTicks.toInt(),
            amplifier,
            ambient,
            particles,
            icon
        )
    }

    override fun serialize(type: Type, obj: PotionEffect?, node: ConfigurationNode) {
        obj?.let {
            node.node("type").set(it.type.name)
            node.node("duration").set(Ticks.duration(it.duration.toLong()))
            node.node("amplifier").set(it.amplifier)
            node.node("ambient").set(it.isAmbient)
            node.node("particles").set(it.hasParticles())
            node.node("icon").set(it.hasIcon())
        } ?: node.raw(null)
    }
}
