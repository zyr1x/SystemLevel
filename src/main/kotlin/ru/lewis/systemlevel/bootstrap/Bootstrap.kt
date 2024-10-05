package ru.lewis.systemlevel.bootstrap

import com.google.inject.Guice
import com.google.inject.Stage
import me.lucko.helper.plugin.ExtendedJavaPlugin
import ru.lewis.systemlevel.Main

class Bootstrap : ExtendedJavaPlugin() {

    private var disabled: Boolean = false

    private lateinit var entryPoint: Main

    override fun enable() {
        try {
            entryPoint = Guice.createInjector(Stage.PRODUCTION, InjectionModule(this)).getInstance(Main::class.java)
            entryPoint.start()
        } catch (e: Throwable) {
            slF4JLogger.error("Failed to enable", e)
            server.scheduler.runTask(this, Runnable { server.pluginManager.disablePlugin(this) })
        }
    }

    override fun disable() {
        if (::entryPoint.isInitialized && !disabled) {
            disabled = true
            entryPoint.stop()
        }
    }
}
