package net.proxyline.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProxiesResponse(
    val count: Int,
    val next: String? = null,
    val previous: String? = null,
    val results: List<ProxyItem> = emptyList()
)

@Serializable
data class ProxyItem(
    val id: Long,
    val ip: String,
    @SerialName("internal_ip")
    val internalIp: String? = null,
    @SerialName("port_http")
    val portHttp: Int,
    @SerialName("port_socks5")
    val portSocks5: Int,
    // Some APIs provide both user and username fields; keep both for completeness
    val user: String? = null,
    val username: String? = null,
    val password: String? = null,
    @SerialName("order_id")
    val orderId: Long? = null,
    // API uses string values like "1"/"2" for type in example; keep as String
    val type: String? = null,
    @SerialName("ip_version")
    val ipVersion: Int? = null,
    val country: String? = null,
    // Keep dates as strings; caller may parse into temporal types if needed
    val date: String? = null,
    @SerialName("date_end")
    val dateEnd: String? = null,
    val tags: List<ProxyTag> = emptyList(),
    @SerialName("access_ips")
    val accessIps: List<String> = emptyList()
)

@Serializable
data class ProxyTag(
    val id: Long,
    val name: String
)
