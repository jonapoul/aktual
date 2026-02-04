package aktual.api.client

import aktual.api.model.internal.AktualHeaders
import aktual.budget.model.BudgetId
import aktual.core.model.Protocol
import aktual.core.model.ServerUrl
import aktual.core.model.Token
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.http.URLProtocol
import io.ktor.http.contentLength
import io.ktor.http.path
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.remaining
import io.ktor.utils.io.exhausted
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import okio.FileSystem
import okio.Path
import okio.buffer
import kotlinx.io.Source as KxSource
import okio.Sink as OkioSink

/**
 * Extracted out of [SyncApi] to minimise hassle in KSP-generating download progress logic.
 */
class SyncDownloadApi(
  private val serverUrl: ServerUrl,
  private val client: HttpClient,
  private val fileSystem: FileSystem,
) : AutoCloseable by client {
  private val urlProtocol = when (serverUrl.protocol) {
    Protocol.Http -> URLProtocol.HTTP
    Protocol.Https -> URLProtocol.HTTPS
  }

  fun downloadUserFile(
    token: Token,
    budgetId: BudgetId,
    path: Path,
  ): Flow<SyncDownloadState> = channelFlow {
    val statement = client.prepareGet {
      url {
        protocol = urlProtocol
        host = serverUrl.baseUrl
        path("/sync/download-user-file")
      }
      header(AktualHeaders.TOKEN, token)
      header(AktualHeaders.FILE_ID, budgetId)
    }

    statement.execute { response ->
      val channel = response.body<ByteReadChannel>()
      val contentLength = response.contentLength() ?: error("No content length in response? $response")
      val sink = fileSystem.sink(path)
      var count = 0L
      sink.use { s ->
        while (!channel.exhausted()) {
          val chunk = channel.readRemaining(CHANNEL_BUFFER_SIZE)
          count += chunk.remaining
          chunk.transferTo(s)
          send(SyncDownloadState.InProgress(count, contentLength))
        }
      }
      send(SyncDownloadState.Done(path, contentLength))
    }
  }

  /**
   * Bridging between [kotlinx.io.Source] and [okio.Sink], since they don't interoperate out the box
   */
  private fun KxSource.transferTo(sink: OkioSink) {
    val buffer = ByteArray(SINK_BUFFER_SIZE)
    val bufferedSink = sink.buffer()
    while (true) {
      val bytesRead = readAtMostTo(buffer, 0, buffer.size)
      if (bytesRead <= 0) break
      bufferedSink.write(buffer, 0, bytesRead)
    }
    bufferedSink.flush()
  }

  private companion object {
    const val CHANNEL_BUFFER_SIZE = 8192L
    const val SINK_BUFFER_SIZE = 4096
  }
}

sealed interface SyncDownloadState {
  data class InProgress(
    val bytesSentTotal: Long,
    val contentLength: Long,
  ) : SyncDownloadState

  data class Done(
    val path: Path,
    val contentLength: Long,
  ) : SyncDownloadState
}
