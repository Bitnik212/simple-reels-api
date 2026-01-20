package net.proxyline.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import net.proxyline.model.ProxyStatus
import net.proxyline.model.ProxyTypeEnum
import net.proxyline.model.ProxiesResponse


class ProxyLineClient(
    private val apiKey: String,
    private val logLevel: LogLevel = LogLevel.NONE
) {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint = true
                    ignoreUnknownKeys = true
                }
            )
        }
        install(Logging) {
            level = logLevel
        }
    }

    suspend fun proxies(
        type: ProxyTypeEnum? = null,
        status: ProxyStatus? = null,
        countries: List<String>? = null,
        ids: List<String>? = null,
        limit: Int = 10,
        offset: Int = 0
    ): ProxiesResponse {
        val response = client.get("https://panel.proxyline.net/api/proxies/") {
            parameter("api_key", apiKey)
            type?.let {
                parameter("type", type.alias)
            }
            status?.let {
                parameter("status", status.alias)
            }
            countries?.forEach {
                parameter("country", it)
            }
            ids?.forEach {
                parameter("ids", it)
            }
            parameter("limit", limit)
            parameter("offset", offset)
        }
        return response.body<ProxiesResponse>()
    }


}