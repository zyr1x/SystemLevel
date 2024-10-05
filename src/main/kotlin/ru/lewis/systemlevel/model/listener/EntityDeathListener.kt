package ru.lewis.systemlevel.model.listener

import jakarta.inject.Inject
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import ru.lewis.systemlevel.extension.randomNumber
import ru.lewis.systemlevel.service.ConfigurationService
import ru.lewis.systemlevel.service.LevelService

class EntityDeathListener @Inject constructor(
    private val configurationService: ConfigurationService,
    private val levelService: LevelService
) : Listener {

    private val levelsConfiguration get() = configurationService.levels
    private val entityConfiguration get() = levelsConfiguration.experienceUpdateQuest.entity

    @EventHandler
    fun onDeath(event: EntityDeathEvent) {

        if (event.entity.killer != null && event.entity.killer is Player) {

            val entityType = event.entityType
            val player = event.entity.killer as Player
            val playerLevel = levelService.getPlayer(player)

            if (entityConfiguration.other.containsKey(entityType)) {

                val entityExp = entityConfiguration.other[entityType] ?: throw NullPointerException("Произошла внутреняя ошибка")

                playerLevel.updateExp(randomNumber(entityExp.min, entityExp.max))

            } else {

                val default = entityConfiguration.default

                if (default != null) {
                    playerLevel.updateExp(randomNumber(default.min, default.max))
                }

            }
        }

    }

}