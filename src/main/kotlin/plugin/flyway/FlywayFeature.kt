package moe.bitt.plugin.flyway

import io.ktor.server.application.Application
import io.ktor.server.config.ApplicationConfigurationException
import io.ktor.util.*
import org.flywaydb.core.Flyway
import org.slf4j.Logger
import io.ktor.server.application.Plugin
import io.ktor.util.logging.KtorSimpleLogger

private val flyWayFeatureLogger: Logger = KtorSimpleLogger("moe.bitt.plugin.flyway.Flyway")


class FlywayFeature(configuration: Configuration) {

    private val locations = configuration.locations
    private val schemas = configuration.schemas
    private val commands: Set<FlywayCommand> = configuration.commands

    data class DatabaseConfig(
        val url: String,
        val user: String,
        val password: String
    )

    class Configuration {
        var locations: Array<String>? = null
        var schemas: Array<String>? = null
        var databaseConfig: DatabaseConfig? = null
        internal var commands: Set<FlywayCommand> = setOf(Info, Migrate)
        fun commands(vararg commandsToExecute: FlywayCommand) {
            commands = commandsToExecute.toSet()
        }

        fun database(url: String, user: String, password: String) {
            databaseConfig = DatabaseConfig(
                url, user, password
            )
        }
    }

    companion object Feature : Plugin<Application, Configuration, FlywayFeature> {
        override val key = AttributeKey<FlywayFeature>("FlywayFeature")

        override fun install(pipeline: Application, configure: Configuration.() -> Unit): FlywayFeature {
            val configuration = Configuration().apply(configure)
            val flywayFeature = FlywayFeature(configuration)
            if (configuration.databaseConfig == null) throw ApplicationConfigurationException("DataBase is not configured")

            flyWayFeatureLogger.info("Flyway migration has started")

            val flyway = Flyway
                .configure(pipeline.environment.classLoader)
                .dataSource(
                    configuration.databaseConfig!!.url,
                    configuration.databaseConfig!!.user,
                    configuration.databaseConfig!!.password
                )
                .also { config -> flywayFeature.locations?.let { config.locations(*it) } }
                .also { config -> flywayFeature.schemas?.let { config.schemas(*it) } }
                .load()

            flywayFeature.commands.map { command ->
                flyWayFeatureLogger.info("Running command: ${command.javaClass.simpleName}")
                command.run(flyway)
            }

            flyWayFeatureLogger.info("Flyway migration has finished")
            return flywayFeature
        }
    }
}
