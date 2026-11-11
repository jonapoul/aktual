package aktual.core.openid

import aktual.core.l10n.Res
import aktual.core.l10n.StringLookup
import aktual.core.l10n.login_openid_done
import aktual.core.model.Token
import aktual.core.utils.OpenIdRedirects
import aktual.core.utils.OpenIdRedirects.Companion.HOST
import aktual.core.utils.OpenIdRedirects.Companion.PATH
import aktual.core.utils.OpenIdRedirects.Companion.TOKEN
import aktual.di.AppScope
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import java.net.HttpURLConnection.HTTP_BAD_REQUEST
import java.net.HttpURLConnection.HTTP_OK
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.URLDecoder
import kotlin.concurrent.thread
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// Like upstream's Electron app, listens on a local port until the browser brings the token back
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class JvmOpenIdRedirects internal constructor(private val strings: StringLookup) : OpenIdRedirects {
  private val received = Channel<Token>(CONFLATED)
  private val mutex = Mutex()
  private var server: HttpServer? = null

  override val tokens: Flow<Token> = received.receiveAsFlow()

  override suspend fun returnUrl(): String = mutex.withLock {
    val running = server ?: start(strings[Res.string.login_openid_done]).also { server = it }
    "http://$HOST:${running.address.port}"
  }

  private fun start(doneMessage: String): HttpServer =
    HttpServer.create(InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0).apply {
      createContext(PATH) { exchange -> handle(exchange, doneMessage) }
      start()
    }

  private fun HttpServer.handle(exchange: HttpExchange, doneMessage: String) {
    val token = exchange.requestURI.rawQuery?.let(::tokenFrom)
    if (token == null) {
      exchange.respond(HTTP_BAD_REQUEST, "")
      return
    }
    exchange.respond(HTTP_OK, doneMessage)
    received.trySend(token)
    stopLater()
  }

  // stop() waits for running exchanges, including the one calling it
  private fun HttpServer.stopLater() {
    server = null
    thread(isDaemon = true) { stop(0) }
  }

  private fun HttpExchange.respond(code: Int, message: String) {
    val bytes = message.toByteArray()
    responseHeaders.add("Content-Type", "text/plain; charset=utf-8")
    sendResponseHeaders(code, if (bytes.isEmpty()) -1 else bytes.size.toLong())
    responseBody.use { it.write(bytes) }
  }

  private fun tokenFrom(query: String): Token? =
    query
      .split('&')
      .map { it.split('=', limit = 2) }
      .firstOrNull { it.size == 2 && it[0] == TOKEN }
      ?.let { URLDecoder.decode(it[1], Charsets.UTF_8) }
      ?.takeIf { it.isNotEmpty() }
      ?.let(::Token)
}
