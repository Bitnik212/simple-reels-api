package moe.bitt.plugin.sentry

import io.ktor.client.plugins.logging.LogLevel
import io.ktor.server.application.*
import io.ktor.server.config.ApplicationConfig
import moe.bitt.getKtorLogLevel


fun Application.configureSentry(config: ApplicationConfig, isDevelopment: Boolean = true) {
    install(SentryFeature) {
        setDSN(
            dsn = config.property("sentry.dsn").getString()
        )
        setIsDevelopment(
            isDevelopment = getKtorLogLevel() == LogLevel.ALL
        )
    }
}
