package ru.lewis.systemlevel.configuration.serializer

import jakarta.inject.Inject
import net.kyori.adventure.bossbar.BossBar
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.extensions.getList
import org.spongepowered.configurate.serialize.TypeSerializer
import ru.lewis.systemlevel.configuration.type.BossBarConfiguration
import java.lang.reflect.Type

class BossBarConfigurationSerializer @Inject constructor() : TypeSerializer<BossBarConfiguration> {

    override fun deserialize(type: Type, node: ConfigurationNode): BossBarConfiguration {
        return BossBarConfiguration(
            node.node("content").string!!,
            node.node("progress").getFloat(Float.NaN).takeUnless { it.isNaN() },
            node.node("color").get(BossBar.Color::class)!!,
            node.node("overlay").get(BossBar.Overlay::class)!!,
            node.node("flags").getList(BossBar.Flag::class)?.toSet()
        )
    }

    override fun serialize(type: Type, obj: BossBarConfiguration?, node: ConfigurationNode) {
        node.node("content").set(obj?.content)
        node.node("progress").set(obj?.progress)
        node.node("color").set(obj?.color)
        node.node("overlay").set(obj?.overlay)
        node.node("flags").setList(BossBar.Flag::class.java, obj?.flags?.toList())
    }
}
