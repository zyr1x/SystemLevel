package ru.lewis.systemlevel.service

import com.google.inject.Inject
import com.google.inject.Singleton
import me.lucko.helper.terminable.TerminableConsumer
import me.lucko.helper.terminable.module.TerminableModule
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.slf4j.Logger
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.extensions.set
import org.spongepowered.configurate.kotlin.objectMapperFactory
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import ru.lewis.systemlevel.configuration.LevelsConfiguration
import ru.lewis.systemlevel.configuration.MessagesConfiguration
import ru.lewis.systemlevel.configuration.serializer.*
import ru.lewis.systemlevel.configuration.type.BossBarConfiguration
import ru.lewis.systemlevel.configuration.Configuration
import ru.lewis.systemlevel.configuration.GuiConfiguration
import ru.lewis.systemlevel.configuration.type.MiniMessageComponent
import java.awt.Color
import java.time.Duration
import kotlin.io.path.*

@Singleton
class ConfigurationService @Inject constructor(
    private val plugin: Plugin,
    private val durationSerializer: DurationSerializer,
    private val miniMessageComponentSerializer: MiniMessageComponentSerializer,
    private val colorSerializer: ColorSerializer,
    private val potionEffectSerializer: PotionEffectSerializer,
    private val bossBarConfigurationSerializer: BossBarConfigurationSerializer,
    private val logger: Logger,
    private val potionEffectTypeSerializer: PotionEffectTypeSerializer,
    private val materialSerializer: MaterialSerializer,
    private val entityTypeSerializer: EntityTypeSerializer,
    private val enchantmentSerializer: EnchantmentSerializer
) : TerminableModule {

    lateinit var menu: GuiConfiguration

    lateinit var config: Configuration
        private set

    lateinit var levels: LevelsConfiguration
        private set

    lateinit var messages: MessagesConfiguration
        private set

    private val rootDirectory = Path("")
    private val menuFile = plugin.dataFolder.toPath().resolve("menu.yml")
    private val configFile = plugin.dataFolder.toPath().resolve("config.yml")
    private val levelsFile = plugin.dataFolder.toPath().resolve("levels.yml")
    private val messagesFile = plugin.dataFolder.toPath().resolve("messages.yml")

    override fun setup(consumer: TerminableConsumer) = doReload()

    fun reload() = doReload()

    fun saveConfig() {

        createLoaderBuilder().path(menuFile).build().let {
            it.save(it.createNode().set(menu))
        }

        createLoaderBuilder().path(configFile).build().let {
            it.save(it.createNode().set(config))
        }

        createLoaderBuilder().path(levelsFile).build().let {
            it.save(it.createNode().set(levels))
        }

        createLoaderBuilder().path(messagesFile).build().let {
            it.save(it.createNode().set(messages))
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Internal
    ///////////////////////////////////////////////////////////////////////////

    @Synchronized
    private fun doReload() {
        plugin.dataFolder.toPath().createDirectories()

        menu = createLoaderBuilder().path(menuFile).build().getAndSave<GuiConfiguration>()
        levels = createLoaderBuilder().path(levelsFile).build().getAndSave<LevelsConfiguration>()
        messages = createLoaderBuilder().path(messagesFile).build().getAndSave<MessagesConfiguration>()
        config = createLoaderBuilder().path(configFile).build().getAndSave<Configuration>()

    }

    private fun createLoaderBuilder(): YamlConfigurationLoader.Builder {
        return YamlConfigurationLoader.builder()

            .defaultOptions {
                it.serializers { serializers ->
                    serializers
                        .register(MiniMessageComponent::class.java, miniMessageComponentSerializer)
                        .register(Duration::class.java, durationSerializer)
                        .register(Color::class.java, colorSerializer)
                        .register(PotionEffect::class.java, potionEffectSerializer)
                        .register(BossBarConfiguration::class.java, bossBarConfigurationSerializer)
                        .register(Material::class.java, materialSerializer)
                        .register(PotionEffectType::class.java, potionEffectTypeSerializer)
                        .register(EntityType::class.java, entityTypeSerializer)
                        .register(Enchantment::class.java, enchantmentSerializer)
                        .registerAnnotatedObjects(objectMapperFactory())
                }
            }

            .indent(2)
            .nodeStyle(NodeStyle.BLOCK)
    }

    private inline fun <reified T : Any> YamlConfigurationLoader.getAndSave(): T {
        val obj = this.load().get(T::class)!!
        this.save(this.createNode().set(T::class, obj))
        return obj
    }

    //powered by BlackBoroness

}

