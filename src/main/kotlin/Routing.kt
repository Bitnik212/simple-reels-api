package moe.bitt

import io.ktor.server.application.*
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.*
import moe.bitt.reels.api.ReelService


fun Application.configureRouting() {

    val service = ReelService()

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
