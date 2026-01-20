package moe.bitt

import io.ktor.client.engine.ProxyBuilder
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.server.application.ApplicationCall
import io.ktor.server.config.ApplicationConfigValue
import io.ktor.server.response.respondText
import io.ktor.server.routing.RoutingCall
import kotlinx.serialization.json.Json
import java.net.Proxy
import java.net.URL

suspend inline fun <reified T> RoutingCall.respondJson(data: T, json: Json = Json, status: HttpStatusCode = HttpStatusCode.OK): Unit {
    respondText(
        text = json.encodeToString(data),
        contentType = ContentType.Application.Json,
        status = status
    )
}

suspend fun RoutingCall.respondError(message: String, json: Json = Json, status: HttpStatusCode = HttpStatusCode.BadRequest): Unit {
    respondJson(
        data = mapOf("message" to message),
        json = json,
        status = status
    )
}

suspend fun ApplicationCall.respondError(message: String, json: Json = Json, status: HttpStatusCode = HttpStatusCode.BadRequest) {
    respondText(
        text = json.encodeToString(mapOf("message" to message)),
        contentType = ContentType.Application.Json,
        status = status
    )
}

fun ApplicationConfigValue.getURLOrNull(): URL? {
    return getString().takeIf { it != "-" }?.let { URL(it) }
}

fun getKtorLogLevel(): LogLevel {
    return System.getenv("KTOR_LOG_LEVEL")?.let {
        when(it) {
            "DEBUG" -> LogLevel.ALL
            "WARN" -> LogLevel.HEADERS
            "INFO" -> LogLevel.NONE
            else -> LogLevel.NONE
        }
    } ?: LogLevel.NONE
}

fun URL.toSocksProxy(): Proxy {
    return ProxyBuilder.socks(
        host = host,
        port = port,
    )
}

fun URL.toHttpProxy(): Proxy {
    return ProxyBuilder.http(Url(this.toURI()))
}

fun Headers.langHeader(): String {
    return when(this["Accept-Language"]) {
        "ko" -> "kr"
        "en" -> "en"
        "ru" -> "ru"
        else -> "kr"
    }
}

