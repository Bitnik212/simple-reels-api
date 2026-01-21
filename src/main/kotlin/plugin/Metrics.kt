package moe.bitt.plugin

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheus.PrometheusMeterRegistry
import org.koin.ktor.ext.inject


fun Application.configureMetrics() {
    val appMicrometerRegistry: PrometheusMeterRegistry by inject<PrometheusMeterRegistry>()
    install(MicrometerMetrics) {
        meterBinders = listOf(
            JvmMemoryMetrics(),
            JvmGcMetrics(),
            ProcessorMetrics()
        )
        registry = appMicrometerRegistry
    }

    routing {
        get("/metrics") {
            call.respond(appMicrometerRegistry.scrape())
        }
    }
}
