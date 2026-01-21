package moe.bitt.plugin.flyway

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.config.ApplicationConfig


fun Application.configureFlyway(config: ApplicationConfig) {
    install(FlywayFeature.Feature) {
        commands(Info, Migrate)
        database(
            url = config.property("database.url").getString(),
            user = config.property("database.user").getString(),
            password = config.property("database.password").getString(),
        )
    }
}
