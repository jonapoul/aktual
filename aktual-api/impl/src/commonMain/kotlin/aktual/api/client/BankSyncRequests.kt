package aktual.api.client

import aktual.budget.model.AccountSyncSource
import aktual.core.model.BudgetServer
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.plugins.timeout
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.path
import kotlin.time.Duration

/** POSTs [body] as JSON to [source]'s [endpoint] on [remote], timing out after [timeout] if set. */
internal inline fun <reified T : Any> HttpRequestBuilder.bankSyncRequest(
  remote: BudgetServer.Remote,
  source: AccountSyncSource,
  endpoint: String,
  body: T,
  timeout: Duration? = null,
) {
  url {
    protocol = remote.url.protocol()
    host = remote.url.baseUrl
    path(source.serverPath(), endpoint)
  }
  header(AktualHeaders.TOKEN, remote.token)

  if (timeout != null) {
    timeout {
      requestTimeoutMillis = timeout.inWholeMilliseconds
      socketTimeoutMillis = timeout.inWholeMilliseconds
    }
  }

  // Rejections like an unconfigured provider come back as 4xx with the usual JSON envelope
  expectSuccess = false
  contentType(ContentType.Application.Json)
  setBody(body)
}

// Null if the request timed out
internal inline fun <T> timingOut(block: () -> T): T? =
  try {
    block()
  } catch (_: HttpRequestTimeoutException) {
    null
  } catch (_: SocketTimeoutException) {
    null
  } catch (_: ConnectTimeoutException) {
    null
  }

internal fun BudgetServer.remote(): BudgetServer.Remote =
  checkNotNull(this as? BudgetServer.Remote) { "No server for bank sync" }

// packages/sync-server/src/app.ts
internal fun AccountSyncSource.serverPath(): String =
  when (this) {
    GoCardless -> "gocardless"
    SimpleFin -> "simplefin"
    PluggyAi -> "pluggyai"
    Akahu -> "akahu"
    EnableBanking -> "enablebanking"
    else -> throw IllegalArgumentException("Unsupported bank sync source $this")
  }
