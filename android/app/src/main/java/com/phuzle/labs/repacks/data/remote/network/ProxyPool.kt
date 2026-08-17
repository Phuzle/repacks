package com.phuzle.labs.repacks.data.remote.network

import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URI

/** A single user-supplied proxy entry, parsed from Configure → Anti-Block's proxy list textbox. */
data class ProxyConfig(
    val proxy: Proxy,
    val username: String?,
    val password: String?,
    val raw: String,
)

/**
 * Parses user-supplied proxy addresses (Configure → Anti-Block) into OkHttp-ready [ProxyConfig]s.
 * Accepted formats: `host:port`, `http://host:port`, `socks5://host:port`, and either with
 * `user:pass@` credentials. The app ships no proxies of its own — see PRD addendum on anti-block
 * strategy — this only understands addresses the user has entered themselves.
 */
object ProxyPool {

    fun parse(rawLines: List<String>): List<ProxyConfig> = rawLines.mapNotNull(::parseOne)

    private fun parseOne(line: String): ProxyConfig? {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) return null
        val normalized = if ("://" in trimmed) trimmed else "http://$trimmed"
        val uri = runCatching { URI(normalized) }.getOrNull() ?: return null
        val host = uri.host ?: return null
        val port = if (uri.port != -1) uri.port else 8080
        val type = when (uri.scheme?.lowercase()) {
            "socks", "socks4", "socks5" -> Proxy.Type.SOCKS
            else -> Proxy.Type.HTTP
        }
        val userInfoParts = uri.userInfo?.split(":", limit = 2)
        val address = InetSocketAddress.createUnresolved(host, port)
        return ProxyConfig(
            proxy = Proxy(type, address),
            username = userInfoParts?.getOrNull(0),
            password = userInfoParts?.getOrNull(1),
            raw = "$host:$port",
        )
    }
}
