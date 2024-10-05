package ru.lewis.systemlevel.model.listener

import jakarta.inject.Inject
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import ru.lewis.systemlevel.extension.randomNumber
import ru.lewis.systemlevel.service.ConfigurationService
import ru.lewis.systemlevel.service.LevelService

class BlockBreakListener @Inject constructor(
    private val configurationService: ConfigurationService,
    private val levelService: LevelService
) : Listener {

    private val blockConfiguration get() = configurationService.levels.experienceUpdateQuest.blocks

    @EventHandler
    fun onBreak(event: BlockBreakEvent) {

            val blockType = event.block.type
        val player = event.player
        val playerLevel = levelService.getPlayer(player)


            if (blockConfiguration.other.containsKey(blockType)) {

                val entityExp = blockConfiguration.other[blockType] ?: throw NullPointerException("Произошла внутреняя ошибка")

                playerLevel.updateExp(randomNumber(entityExp.min, entityExp.max))

            } else {

                val default = blockConfiguration.default

                if (default != null) {
                    playerLevel.updateExp(randomNumber(default.min, default.max))
                }

            }

    }

}