package moe.bitt

import io.ktor.server.application.*
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import moe.bitt.reels.api.ReelService
import net.proxyline.client.ProxyLineClient
import net.proxyline.service.ProxyLineService
import org.koin.ktor.ext.inject
import java.net.Proxy


fun Application.configureRouting() {

    val config by inject<ApplicationConfig>()

    val rotatingProxies = config.propertyOrNull("proxyline.api-key")?.getString()?.takeIf { it.isNotBlank() }?.let { apiKey ->
        val rotatingProxies = mutableMapOf<Proxy, String>()
        val proxyLineService = ProxyLineService(
            ProxyLineClient(
                apiKey = apiKey,
                hostname = config.property("proxyline.hostname").getString()
            )
        )
        runBlocking {
            try {
                proxyLineService.httpProxiesUrlByTag(config.property("proxyline.tag").getString()).forEach {
                    rotatingProxies[it.toHttpProxy()] = it.authority
                }
                rotatingProxies.toMap()
            } catch (e: Exception) {
                println("ProxyLine API error: ${e.message}")
                null
            }
        }
    } ?: emptyMap()

    val service = ReelService(
        rotatingProxies = rotatingProxies
    )

    routing {
        get("/reels/{reel_id}") {
            val reelId = call.parameters["reel_id"] ?: return@get call.respondError("Invalid reel_id")
            call.respondJson(
                data = service.info(reelId = reelId).shortCodeMedia
            )
        }

        post("/reels/{reel_id}") {
            val isRedirect = call.parameters["redirect"]?.toBoolean() ?: true
            val reelId = call.parameters["reel_id"] ?: return@post call.respondError("Invalid reel_id")
            service.download(reelId = reelId)?.also { url ->
                if (isRedirect) {
                    call.respondRedirect(url=url)
                } else {
                    call.respondJson(
                        data = mapOf("url" to url)
                    )
                }
            } ?: call.respondError("Reel not found")
        }
    }
}
