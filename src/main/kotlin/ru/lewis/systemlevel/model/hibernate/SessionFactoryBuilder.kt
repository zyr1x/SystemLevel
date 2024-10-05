package ru.lewis.systemlevel.model.hibernate

import jakarta.persistence.AttributeConverter
import org.hibernate.SessionFactory
import org.hibernate.cache.spi.RegionFactory
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider
import org.hibernate.hikaricp.internal.HikariCPConnectionProvider
import org.hibernate.tool.schema.Action
import org.hibernate.cfg.Configuration
import org.hibernate.cfg.Environment
import org.hibernate.hikaricp.internal.HikariConfigurationUtil
import java.sql.Driver
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass

class SessionFactoryBuilder(
    // Connect info
    var user: String? = null,
    var password: String? = null,
    var url: String? = null,
    var driver: KClass<out Driver>? = null,

    // Cache
    var useQueryCache: Boolean? = null,
    var cacheRegionFactory: KClass<out RegionFactory>? = null,

    // Properties
    var hikariProperties: MutableMap<String, String> = mutableMapOf(),
    var customProperties: MutableMap<String, String> = mutableMapOf(),

    // Misc
    var enableSqlLogging: Boolean? = null,
    var annotatedClasses: MutableSet<KClass<*>> = mutableSetOf(),
    var classLoader: ClassLoader? = null,
    var connectionProvider: KClass<out ConnectionProvider>? = HikariCPConnectionProvider::class,
    var hmd2ddlAuto: Action? = Action.UPDATE,
    var globalAttributeConverters: MutableCollection<AttributeConverter<*, *>> = mutableListOf()
) {

    fun build(): SessionFactory {
        val future = CompletableFuture<SessionFactory>()
        val thread = Thread { future.complete(build0()) }
        classLoader?.also { thread.contextClassLoader = it }
        thread.start()
        return future.join()
    }

    inline fun <reified T> register() {
        annotatedClasses.add(T::class)
    }

    private fun build0(): SessionFactory {
        val configuration = Configuration()

        user?.also { configuration.setProperty(Environment.JAKARTA_JDBC_USER, it) }
        password?.also { configuration.setProperty(Environment.JAKARTA_JDBC_PASSWORD, it) }
        url?.also { configuration.setProperty(Environment.JAKARTA_JDBC_URL, it) }
        driver?.also { configuration.setProperty(Environment.JAKARTA_JDBC_DRIVER, it.qualifiedName) }
        connectionProvider?.also { configuration.setProperty(Environment.CONNECTION_PROVIDER, it.qualifiedName) }
        hmd2ddlAuto?.also { configuration.setProperty(Environment.HBM2DDL_AUTO, it.externalHbm2ddlName) }

        useQueryCache?.also { configuration.setProperty(Environment.USE_QUERY_CACHE, it.toString()) }
        cacheRegionFactory?.also { configuration.setProperty(Environment.CACHE_REGION_FACTORY, it.qualifiedName) }

        enableSqlLogging?.also {
            configuration.setProperty(Environment.SHOW_SQL, it.toString())
            configuration.setProperty(Environment.HIGHLIGHT_SQL, it.toString())
        }

        annotatedClasses.forEach {
            configuration.addAnnotatedClass(it.java)
        }

        customProperties.putAll(hikariProperties.mapKeys { "${HikariConfigurationUtil.CONFIG_PREFIX}${it.key}" })
        customProperties.forEach { (key, value) -> configuration.setProperty(key, value) }

        globalAttributeConverters.forEach { configuration.addAttributeConverter(it) }

        return configuration.buildSessionFactory()
    }

    companion object {

        inline fun build(builder: SessionFactoryBuilder.() -> Unit): SessionFactory {
            return SessionFactoryBuilder()
                .also(builder)
                .build()
        }
    }
}