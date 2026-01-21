package moe.bitt.plugin.sentry

import io.ktor.server.application.Application
import io.ktor.server.application.Plugin
import io.ktor.util.AttributeKey
import io.sentry.Sentry


class SentryFeature(config: SentryConfig) {

    companion object Feature: Plugin<Application, SentryConfig, SentryFeature> {
        override val key: AttributeKey<SentryFeature> = AttributeKey<SentryFeature>("SentryPlugin")

        override fun install(
            pipeline: Application,
            configure: SentryConfig.() -> Unit
        ): SentryFeature {
            val configuration = SentryConfig().apply(configure)
            val sentryPlugin = SentryFeature(configuration)

            Sentry.init { options ->
                options.dsn = configuration.dsn
                // Set tracesSampleRate to 1.0 to capture 100% of transactions for performance monitoring.
                // We recommend adjusting this value in production.
                options.tracesSampleRate = 1.0
                // When first trying Sentry it's good to see what the SDK is doing:
                options.isDebug = configuration.isDevelopment
                options.addInAppInclude("md.automenu.api")
                options.environment = if (configuration.isDevelopment) "development" else "production"
            }

            return sentryPlugin
        }
    }

}
