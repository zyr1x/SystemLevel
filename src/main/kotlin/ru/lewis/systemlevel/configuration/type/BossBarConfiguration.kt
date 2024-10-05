package ru.lewis.systemlevel.configuration.type

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import ru.lewis.systemlevel.extension.parseMiniMessage

data class BossBarConfiguration(
    val content: String,
    val progress: Float? = null,
    val color: BossBar.Color,
    val overlay: BossBar.Overlay,
    val flags: Set<BossBar.Flag>? = null,
) {

    fun createBossBar(vararg tagResolvers: TagResolver): BossBarReference = BossBarReference(this, tagResolvers)

    class BossBarReference(val config: BossBarConfiguration, tagResolvers: Array<out TagResolver>) {

        val bar = BossBar.bossBar(
            config.content.parseMiniMessage(*tagResolvers),
            config.progress ?: 0f, config.color, config.overlay, config.flags ?: setOf()
        )

        fun updateContent(vararg tagResolvers: TagResolver) {
            this.bar.name(config.content.parseMiniMessage(*tagResolvers))
        }

        fun updateProgressPercent(current: Number, max: Number) {
            this.bar.progress((current.toFloat() / max.toFloat()).coerceIn(0.0f, 1.0f))
        }
    }
}
