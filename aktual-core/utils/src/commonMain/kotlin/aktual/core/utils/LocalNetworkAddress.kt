package aktual.core.utils

// mDNS names, which only resolve over the local network
fun isLocalNetworkHost(host: String): Boolean =
  host.trimEnd('.').endsWith(".local", ignoreCase = true)
