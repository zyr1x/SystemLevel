package ru.lewis.systemlevel.model.level

import com.google.inject.assistedinject.Assisted
import jakarta.inject.Inject
import org.bukkit.entity.Player
import ru.lewis.systemlevel.configuration.type.LevelConfiguration
import ru.lewis.systemlevel.configuration.type.MiniMessageComponent
import ru.lewis.systemlevel.service.LevelService

class PlayerLevel @Inject constructor(
    private val levelService: LevelService,
    @Assisted val player: Player,
    @Assisted var id: Int,
    @Assisted var displayName: MiniMessageComponent,
    @Assisted var currentExp: Double
) {

    fun getNextLevel(): LevelConfiguration {
        return levelService.getNextLevel(player)
    }

    fun updateExp(exp: Double) {
        levelService.updateExp(player, exp)
    }

    fun setExp(exp: Double) {
        levelService.setExp(player, exp)
    }

    fun setLevel(level: Int) {
        levelService.setLevel(player, level)
    }

    fun reset() {
        levelService.reset(player)
    }

    fun save() {
        levelService.save(this)
    }

    fun buyNextLevel(): Boolean {
        return levelService.buyNextLevel(player)
    }

}