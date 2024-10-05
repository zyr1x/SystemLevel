package ru.lewis.systemlevel

import com.google.inject.Inject
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.plugin.Plugin
import org.slf4j.Logger
import ru.lewis.systemlevel.model.SmartLifoCompositeTerminable
import ru.lewis.systemlevel.model.listener.BlockBreakListener
import ru.lewis.systemlevel.model.listener.EntityDeathListener
import ru.lewis.systemlevel.model.placeholder.CustomPlaceHolder
import ru.lewis.systemlevel.service.CommandService
import ru.lewis.systemlevel.service.ConfigurationService
import ru.lewis.systemlevel.service.LevelService
import xyz.xenondevs.invui.InvUI

class Main @Inject constructor(
    private val bukkitAudiences: BukkitAudiences,
    private val configurationService: ConfigurationService,
    private val commandService: CommandService,
    private val levelService: LevelService,
    private val customPlaceHolder: CustomPlaceHolder,
    private val blockBreakListener: BlockBreakListener,
    private val entityDeathListener: EntityDeathListener,
    private val plugin: Plugin,
    logger: Logger,
    ) {

    private val terminableRegistry = SmartLifoCompositeTerminable(logger)

    fun start() {

        InvUI.getInstance().setPlugin(plugin)

        audiences = bukkitAudiences
        terminableRegistry.apply {
            with(bukkitAudiences)
            bindModule(configurationService)
            bindModule(commandService)
            bindModule(levelService)
        }

        plugin.server.pluginManager.registerEvents(blockBreakListener, plugin)
        plugin.server.pluginManager.registerEvents(entityDeathListener, plugin)

        customPlaceHolder.register()
    }

    fun stop() {
        terminableRegistry.closeAndReportException()
        customPlaceHolder.unregister()
    }

    companion object {
        lateinit var audiences: BukkitAudiences
    }
}
