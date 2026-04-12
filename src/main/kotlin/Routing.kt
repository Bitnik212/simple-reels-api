package moe.bitt

import io.bitnik212.instagram.reels.api.ShortcodeMedia
import io.ktor.server.application.*
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.*
import io.sentry.Sentry
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
            try {
                val data = service.info(reelId = reelId)
                data.shortCodeMedia?.also { shortCodeMedia ->
                    reelRepository.insert(reelId = reelId, metadata = shortCodeMedia)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Sentry.captureException(e)
                null
            }
        } else {
            reelRepository.findByReelId(reelId)?.metaData
        }
    }

    routing {
        get("/reels/metadata") {
            val reelIds = call.request.queryParameters["reel_ids"]?.split(",")?.filter { it.isNotBlank() }
                ?: return@get call.respondError("Missing or empty reel_ids query parameter")

            val cachedReels = reelRepository.findByReelIds(reelIds)
            val cachedMap = cachedReels.associateBy { it.reelId }

            val missingIds = reelIds.filter { it !in cachedMap }

            val fetchedMetadata = if (missingIds.isNotEmpty()) {
                coroutineScope {
                    missingIds.map { id ->
                        async {
                            try {
                                saveReelIfNotExist(id)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Sentry.captureException(e)
                                null
                            }
                        }
                    }.awaitAll()
                }.filterNotNull()
            } else {
                emptyList()
            }

            val result = reelIds.mapNotNull { id ->
                cachedMap[id]?.metaData ?: fetchedMetadata.find { it.shortcode == id }
            }

            call.respondJson(result)
        }
    
        get("/reels/metadata/search") {
            val text = call.request.queryParameters["text"] ?: ""
            val shortcodes = call.request.queryParameters["shortcodes"]?.split(",")?.filter { it.isNotBlank() }
            
            if (text.isBlank() && shortcodes.isNullOrEmpty()) {
                return@get call.respondError("Provide either 'text' or 'shortcodes' query parameter")
            }

            val result = reelRepository.search(text = text, shortcodes = shortcodes)
            call.respondJson(result.map { it.metaData })
        }

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
