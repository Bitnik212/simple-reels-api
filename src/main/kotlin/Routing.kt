package moe.bitt

import io.bitnik212.instagram.reels.api.ShortcodeMedia
import io.ktor.server.application.*
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import moe.bitt.reels.api.repository.ReelRepository
import moe.bitt.reels.api.service.ReelService
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

    val reelRepository = ReelRepository()

    suspend fun saveReelIfNotExist(reelId: String): ShortcodeMedia? {
        return if (reelRepository.findByReelId(reelId) == null) {
            val data = service.info(reelId = reelId)
            data.shortCodeMedia?.also { shortCodeMedia ->
                reelRepository.insert(reelId = reelId, metadata = shortCodeMedia)
            }
        } else {
            reelRepository.findByReelId(reelId)?.metaData
        }
    }

    routing {
        get("/reels/{reel_id}") {
            val reelId = call.parameters["reel_id"] ?: return@get call.respondError("Invalid reel_id")
            val metadata: ShortcodeMedia = saveReelIfNotExist(reelId) ?: let {
                return@get call.respondError("Reel not found")
            }
            call.respondJson(
                data = metadata
            )
        }

        post("/reels/{reel_id}") {
            val isRedirect = call.parameters["redirect"]?.toBoolean() ?: true
            val reelId = call.parameters["reel_id"] ?: return@post call.respondError("Invalid reel_id")
            saveReelIfNotExist(reelId) ?: let {
                return@post call.respondError("Reel not found")
            }
            val cachedReel = reelRepository.findByReelId(reelId)
            val url = if (cachedReel?.videoUrl != null) {
                cachedReel.videoUrl
            } else {
                service.download(reelId = reelId)?.also { url ->
                    reelRepository.setVideoUrl(reelId = reelId, videoUrl = url)
                }
            } ?: let { return@post call.respondError("Reel not found") }

            if (isRedirect) {
                call.respondRedirect(url=url)
            } else {
                call.respondJson(
                    data = mapOf("url" to url)
                )
            }

        }
    }
}
