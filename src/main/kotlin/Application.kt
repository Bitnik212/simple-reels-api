package moe.bitt

import io.ktor.server.application.*
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import moe.bitt.plugin.configureDatabases
import moe.bitt.plugin.flyway.configureFlyway
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger


fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    val config = ApplicationConfig("application.yaml")

    val initModule = module {
        single<ApplicationConfig> {
            config
        }
        single<ApplicationEnvironment> {
            environment
        }
    }

    install(Koin) {
        slf4jLogger()
        modules(mainModule, initModule)
    }
    configureDatabases(config)
    configureFlyway(config)
    configureHTTP()
    configureMonitoring()
    configureRouting()
}
