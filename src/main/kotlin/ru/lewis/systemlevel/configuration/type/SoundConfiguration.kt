package ru.lewis.systemlevel.configuration.type

import org.bukkit.Sound
import org.bukkit.entity.Player
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class SoundConfiguration(
    val type: Sound,
    val volume: Float = 1.0f,
    val pitch: Float = 1.0f
) {

    fun play(player: Player) {
        player.playSound(player.location, type, volume, pitch)
    }
}
