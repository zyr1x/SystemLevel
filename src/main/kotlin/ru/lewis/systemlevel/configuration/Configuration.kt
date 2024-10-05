package ru.lewis.systemlevel.configuration

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.time.Duration

@ConfigSerializable
data class Configuration(
    val database: DatabaseConfiguration = DatabaseConfiguration(),
    val performance: Performance = Performance()
) {

    @ConfigSerializable
    data class DatabaseConfiguration(
        val address: String = "localhost",
        val port: Int = 3306,
        val database: String = "mydatabase",
        val user: String = "user",
        val password: String = "password",
        val parameters: List<String> = listOf("useServerPrepStmts=true")
    )

    @ConfigSerializable
    data class Performance(
        val savePeriod: Duration = Duration.ofMinutes(5)
    )


}