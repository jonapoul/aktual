package aktual.di

import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import logcat.logcat
import okhttp3.Call
import okhttp3.EventListener
import okhttp3.HttpUrl
import okhttp3.Protocol

internal class NetworkEventLogger : EventListener() {
  private var callStart: TimeMark? = null
  private var dnsStart: TimeMark? = null

  override fun callStart(call: Call) {
    callStart = TimeSource.Monotonic.markNow()
  }

  override fun proxySelectEnd(call: Call, url: HttpUrl, proxies: List<Proxy>) {
    if (proxies.any { it.type() != DIRECT }) {
      logcat.d { "${call.tag()} proxies=$proxies" }
    }
  }

  override fun dnsStart(call: Call, domainName: String) {
    dnsStart = TimeSource.Monotonic.markNow()
  }

  override fun dnsEnd(call: Call, domainName: String, inetAddressList: List<InetAddress>) {
    val elapsed = dnsStart?.elapsedNow() ?: return
    if (elapsed > SLOW_DNS) {
      logcat.w { "${call.tag()} slow DNS for $domainName: $elapsed -> $inetAddressList" }
    }
  }

  override fun connectFailed(
    call: Call,
    inetSocketAddress: InetSocketAddress,
    proxy: Proxy,
    protocol: Protocol?,
    ioe: IOException,
  ) {
    logcat.w { "${call.tag()} connectFailed $inetSocketAddress proxy=$proxy: $ioe" }
  }

  override fun callFailed(call: Call, ioe: IOException) {
    logcat.w {
      "${call.tag()} callFailed after ${callStart?.elapsedNow()} dnsStarted=${dnsStart != null}: " +
        "${ioe::class.qualifiedName}: ${ioe.message}"
    }
  }

  private fun Call.tag(): String = "${request().method} ${request().url}"

  class Factory : EventListener.Factory {
    override fun create(call: Call) = NetworkEventLogger()
  }

  private companion object {
    val SLOW_DNS = 1.seconds
  }
}
