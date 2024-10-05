package ru.lewis.systemlevel.model.placeholder

import jakarta.inject.Inject
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player
import ru.lewis.systemlevel.extension.legacy
import ru.lewis.systemlevel.service.LevelService
import java.math.RoundingMode

class CustomPlaceHolder @Inject constructor(

    private val levelService: LevelService

) : PlaceholderExpansion() {

    override fun getIdentifier(): String {
        return "systemlevel"
    }

    override fun getAuthor(): String {
        return "Lewis Carrol --> https://t.me/zyr1xx/"
    }

    override fun getVersion(): String {
        return "v1.0"
    }

    override fun onPlaceholderRequest(player: Player?, params: String): String? {

        val levelPlayer = levelService.getPlayer(player!!)

        when (params) {
            "currentLevel" -> {
                return levelPlayer.id.toString()
            }
            "currentExp" -> {
                return levelPlayer.currentExp.toBigDecimal().setScale(1, RoundingMode.DOWN).toString()
            }
            "newLevel" -> {
                return levelPlayer.getNextLevel().toString()
            }
            "expForNewLevel" -> {
                return levelPlayer.getNextLevel().upExp!!.toBigDecimal().setScale(1, RoundingMode.DOWN).toString()
            }
            "displayName" -> {
                return levelPlayer.displayName.legacy()
            }
        }

        return "Ошибка"

    }
}