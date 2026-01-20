package net.proxyline.service

import net.proxyline.client.ProxyLineClient
import net.proxyline.model.ProxyItem
import java.net.URL


class ProxyLineService(
    private val client: ProxyLineClient
) {

    suspend fun socksProxiesUrlByTag(tagName: String): List<URL> {
        val taggedProxies = proxiesUrlByTag(tagName)
        return taggedProxies.map { item -> URL("http://${item.username}:${item.password}@${item.ip}:${item.portSocks5}") }
    }

    suspend fun httpProxiesUrlByTag(tagName: String): List<URL> {
        val taggedProxies = proxiesUrlByTag(tagName)
        return taggedProxies.map { item -> URL("http://${item.username}:${item.password}@${item.ip}:${item.portHttp}") }
    }

    suspend fun proxiesUrlByTag(tagName: String): List<ProxyItem> {
        return client.proxies(limit = 500).results.filter { it.tags.find { tag -> tag.name == tagName } != null }
    }

}