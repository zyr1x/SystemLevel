package ru.lewis.systemlevel.bootstrap

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.assistedinject.FactoryModuleBuilder
import jakarta.inject.Singleton
import me.lucko.helper.plugin.HelperPlugin
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.minimessage.MiniMessage
import org.black_ixx.playerpoints.PlayerPoints
import org.bukkit.Server
import org.bukkit.plugin.Plugin
import org.slf4j.Logger
import ru.lewis.systemlevel.bootstrap.assistedinject.Factory
import ru.lewis.systemlevel.model.level.PlayerLevel

class InjectionModule(
    private val helperPlugin: HelperPlugin
) : AbstractModule() {

    override fun configure() {

        bind(Plugin::class.java).toInstance(helperPlugin)

        install(
            FactoryModuleBuilder()
                .build(Factory::class.java))
    }

    @Singleton
    @Provides
    fun BukkitAudiences(plugin: Plugin): BukkitAudiences = BukkitAudiences.create(plugin)

    @Provides
    fun MiniMessage(): MiniMessage = MiniMessage.miniMessage()

    @Provides
    fun Server(plugin: Plugin): Server = plugin.server

    @Provides
    fun Logger(plugin: Plugin): Logger = plugin.slF4JLogger

}
