package ru.lewis.systemlevel.service

import jakarta.inject.Inject
import jakarta.inject.Singleton
import me.lucko.helper.Events
import me.lucko.helper.Schedulers
import me.lucko.helper.terminable.TerminableConsumer
import me.lucko.helper.terminable.module.TerminableModule
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.black_ixx.playerpoints.PlayerPoints
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin
import org.hibernate.SessionFactory
import ru.lewis.systemlevel.bootstrap.assistedinject.Factory
import ru.lewis.systemlevel.extension.handlerAsync
import ru.lewis.systemlevel.model.hibernate.SessionFactoryBuilder
import ru.lewis.systemlevel.model.hibernate.entity.PlayerHibernateEntity
import ru.lewis.systemlevel.model.level.PlayerLevel
import java.util.UUID
import java.util.concurrent.TimeUnit
import org.slf4j.Logger
import ru.lewis.systemlevel.configuration.type.LevelConfiguration
import ru.lewis.systemlevel.extension.asMiniMessageComponent
import ru.lewis.systemlevel.extension.legacy
import kotlin.time.Duration.Companion.seconds

@Singleton
class LevelService @Inject constructor(

    private val factory: Factory,
    private val configurationService: ConfigurationService,
    private val plugin: Plugin,
    private val logger: Logger

) : TerminableModule {

    private val config get() = configurationService.config
    private val messages get() = configurationService.messages
    private val levelsConfiguration get() = configurationService.levels

    private lateinit var sessionFactory: SessionFactory
    private val players: MutableMap<UUID, PlayerLevel> = mutableMapOf()

    private val ppApi = PlayerPoints.getInstance().api

    override fun setup(consumer: TerminableConsumer) {

        sessionFactory = SessionFactoryBuilder.build {
            classLoader = plugin::class.java.classLoader

            val cfg = config.database

            user = cfg.user
            password = cfg.password
            driver = org.mariadb.jdbc.Driver::class
            url = "jdbc:mariadb://${cfg.address}:${cfg.port}/${cfg.database}${parametersToString(cfg.parameters)}"

            hikariProperties["maximumPoolSize"] = Runtime.getRuntime().availableProcessors().toString()
            hikariProperties["connectionTimeout"] = 10.seconds.inWholeMilliseconds.toString()
            hikariProperties["poolName"] = plugin.name

            register<PlayerHibernateEntity>()

        }
        consumer.bind(sessionFactory)

        // events

        Events.subscribe(PlayerQuitEvent::class.java)
            .filter { players.contains(it.player.uniqueId) }
            .handlerAsync {

                val playerLevel = this.getPlayer(it.player)
                this.save(playerLevel)

                players.remove(playerLevel.player.uniqueId)

            }.bindWith(consumer)

        // scheduler

        Schedulers.builder().async().every(
            config.performance.savePeriod.toSeconds(),
            TimeUnit.SECONDS
        ).run {

            var count = 0;

            for (playerLevel in players.values) {
                this.save(playerLevel)
                count++
            }

            logger.info("Successfully saved $count players in database.")

        }

    }

    fun getNextLevel(player: Player): LevelConfiguration {

        val playerLevel = this.getPlayer(player)

        val nextLevel = levelsConfiguration.levelsSettings.levels.find { it.id == playerLevel.id + 1 }
            ?: levelsConfiguration.levelsSettings.levels.last()

        return nextLevel

    }

    fun buyNextLevel(player: Player): Boolean {

        val nextLevel = getNextLevel(player)

        if (!ppApi.take(player.uniqueId, nextLevel.price!!.toInt())) {
            return false
        }

        val playerLevel = this.getPlayer(player)
        playerLevel.updateExp(nextLevel.upExp!! - playerLevel.currentExp)

        return true
    }

    fun reset(player: Player) {
        val defaultLevel = levelsConfiguration.levelsSettings.defaultLevel
        this.setLevel(player, defaultLevel.id)
        this.setExp(player, 0.0)
    }

    fun updateExp(player: Player, exp: Double) {

        val playerLevel = this.getPlayer(player)
        val id = playerLevel.id
        var remainsExp = exp + playerLevel.currentExp

        for (level in this.levelsConfiguration.levelsSettings.levels) {

            if (level.id <= id) {
                continue
            }

            if (remainsExp >= level.upExp!!) {
                remainsExp -= level.upExp

                level.commands?.forEach {
                    Bukkit.dispatchCommand(
                        Bukkit.getConsoleSender(),
                        it.asMiniMessageComponent().resolve(
                            Placeholder.unparsed(
                                "player",
                                player.name
                            )
                        ).legacy()
                    )
                }

                this.setLevel(player, level.id)
            }

        }

        playerLevel.setExp(remainsExp)
    }

    fun getPlayer(player: Player): PlayerLevel {

        val uuid = player.uniqueId
        val defaultLevel = levelsConfiguration.levelsSettings.defaultLevel

        var playerLevel = this.factory.create(
            player,
            defaultLevel.id,
            defaultLevel.displayName,
            0.0
        )

        if (players.containsKey(uuid)) {

            playerLevel = (players[uuid])!!

        } else {

            sessionFactory.inTransaction { session ->

                val entity = session.find(PlayerHibernateEntity::class.java, uuid)

                if (entity != null) {

                    val currentLevel = levelsConfiguration.levelsSettings.levels.find {
                        it.id == entity.level
                    } ?: defaultLevel

                    playerLevel.apply {
                        id = currentLevel.id
                        currentExp = entity.exp!!
                        displayName = currentLevel.displayName
                    }
                }

                players[uuid] = playerLevel

            }

        }

        return playerLevel

    }

    fun setLevel(player: Player, level: Int) {

        val uniqueId = player.uniqueId
        val playerLevel = this.players[uniqueId]

        if (messages.level.info.showMessage) {

            player.sendMessage(
                messages.level.info.feedBack.resolve(
                    Placeholder.unparsed(
                        "lastlevel",
                        playerLevel!!.id.toString()
                    ),
                    Placeholder.unparsed(
                        "newlevel",
                        level.toString()
                    )
                )
            )

        }

        playerLevel!!.id = level

    }

    fun setExp(player: Player, exp: Double) {

        val uniqueId = player.uniqueId
        val playerLevel = this.players[uniqueId]

        if (messages.exp.info.showMessage) {

            player.sendMessage(
                messages.exp.info.feedBack.resolve(
                    Placeholder.unparsed(
                        "lastexp",
                        playerLevel!!.currentExp.toString()
                    ),
                    Placeholder.unparsed(
                        "newexp",
                        exp.toString()
                    )
                )
            )

        }

        playerLevel!!.currentExp = exp

    }

    fun save(playerLevel: PlayerLevel) {

        val uuid = playerLevel.player.uniqueId

        sessionFactory.inTransaction { session ->

            var entity = session.find(PlayerHibernateEntity::class.java, uuid)

            if (entity != null) {

                entity.level = playerLevel.id
                entity.exp = playerLevel.currentExp

                session.merge(entity)

            } else {

                entity = PlayerHibernateEntity(
                    uuid,
                    playerLevel.id,
                    playerLevel.currentExp
                )

                session.persist(entity)

            }

        }

    }

    private fun parametersToString(parameters: List<String>): String {
        return parameters.joinToString(prefix = "?", separator = "&")
    }

}