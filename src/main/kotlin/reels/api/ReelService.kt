package moe.bitt.reels.api

import io.bitnik212.instagram.reels.api.MediaData
import io.bitnik212.instagram.reels.api.MediaInfoClient
import io.bitnik212.instagram.reels.api.utils.InstagramApiParamsService
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.toMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.Credentials
import okhttp3.Request
import okhttp3.Response
import org.koin.java.KoinJavaComponent.inject
import java.io.Closeable
import java.net.Proxy
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.time.Duration.Companion.seconds


class ReelService(
    rotatingProxies: Map<Proxy, String> = emptyMap()
) : Closeable {

    val s3Client by inject<S3Client>(S3Client::class.java)

    private fun createClient(proxy: Proxy?, login: String? = null, password: String? = null): HttpClient {
        return HttpClient(OkHttp) {

            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        prettyPrint = true
                    }
                )
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 60.seconds.inWholeMilliseconds
                connectTimeoutMillis = 20.seconds.inWholeMilliseconds
                socketTimeoutMillis  = 60.seconds.inWholeMilliseconds
            }

            install(Logging) {
                level = LogLevel.HEADERS
            }

            engine {
                this.proxy = proxy

                config {
                    proxyAuthenticator(object : Authenticator {
                        override fun authenticate(route: okhttp3.Route?, response: Response): Request? {
                            if (login == null || password == null) return response.request.newBuilder().build()
                            val credential = Credentials.basic(login, password)

                            // Avoid infinite auth loops
                            if (response.request.header("Proxy-Authorization") != null) return null

                            return response.request.newBuilder()
                                .header("Proxy-Authorization", credential)
                                .build()
                        }
                    })

                    addInterceptor { chain ->
                        val req = chain.request().newBuilder()
                        when (chain.request().url.host) {
                            "www.instagram.com" -> {
                                MediaInfoClient.DEFAULT_HEADERS.map { (headerName, headerValue) ->
                                    req.addHeader(headerName, headerValue)
                                }
                            }
                        }
                        chain.proceed(req.build())
                    }
                }
            }


            defaultRequest {
                url {
                    host = "www.instagram.com"
                    protocol = URLProtocol.HTTPS
                }
            }
        }
    }

    // Build clients for rotation. If rotatingProxies provided, use them; otherwise fall back to single client with socksProxy (if any)
    private val clients: List<HttpClient> = when {
        rotatingProxies.isNotEmpty() -> rotatingProxies.map { (rotatingProxy, authString) ->
            var (login, password) = authString.split(":")
            password = password.split('@').first()
            createClient(rotatingProxy, login, password)
        }
        else -> {
            listOf(createClient(null))
        }
    }

    private val rrIndex = AtomicInteger(0)

    private fun nextClient(): HttpClient {
        val list = clients
        if (list.size == 1) return list[0]
        val i = rrIndex.getAndUpdate { curr ->
            val next = curr + 1
            if (next >= list.size) 0 else next
        }
        return list[i % list.size]
    }

    suspend fun info(reelId: String): MediaData {
        val client = nextClient()
        val instagramAPI = MediaInfoClient(
            client = client,
            apiParams = InstagramApiParamsService(client)
        )
        val reelInfo = instagramAPI.info(reelId)
        return reelInfo.data
    }

    suspend fun download(reelId: String): String? {
        val client = nextClient()
        return info(reelId).shortCodeMedia?.let { shortCodeMedia ->
            val response = client.get(shortCodeMedia.videoUrl)
            val contentType = response.headers["Content-Type"] ?: ""
            if (contentType.startsWith("text/html")) return@let null
            when(response.status) {
                HttpStatusCode.OK -> {
                    val contentLength = response.headers["Content-Length"]
                    val output = response.bodyAsChannel()

                    s3Client.putChannel(
                        channel = output,
                        contentLength = contentLength?.toLong() ?: 0,
                        key = "video/${reelId}.mp4",
                        contentType = "video/mp4"
                    )
                }
                else -> {
                    null
                }
            }
        } ?: null
    }

    override fun close() {
        clients.forEach { it.close() }
    }

}