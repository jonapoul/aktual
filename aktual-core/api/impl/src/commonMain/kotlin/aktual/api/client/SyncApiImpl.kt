package aktual.api.client

import aktual.api.model.sync.DeleteUserFileRequest
import aktual.api.model.sync.DeleteUserFileResponse
import aktual.api.model.sync.GetUserFileInfoResponse
import aktual.api.model.sync.GetUserKeyRequest
import aktual.api.model.sync.GetUserKeyResponse
import aktual.api.model.sync.ListUserFilesResponse
import aktual.budget.model.BudgetId
import aktual.core.model.ServerUrl
import aktual.core.model.Token
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.prepareGet
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentLength
import io.ktor.http.contentType
import io.ktor.http.path
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.remaining
import io.ktor.utils.io.exhausted
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.io.Source as KxSource
import okio.FileSystem
import okio.Path
import okio.Sink as OkioSink
import okio.buffer

class SyncApiImpl(
  private val client: HttpClient,
  private val fileSystem: FileSystem,
  private val serverUrl: ServerUrl,
) : SyncApi {
  private val urlProtocol = serverUrl.protocol()

  override fun downloadUserFile(
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
      val contentLength =
        response.contentLength() ?: error("No content length in response? $response")
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

  override suspend fun fetchUserFiles(token: Token): ListUserFilesResponse.Success =
    client
      .get {
        url {
          protocol = urlProtocol
          host = serverUrl.baseUrl
          path("/sync/list-user-files")
        }
        header(AktualHeaders.TOKEN, token)
      }
      .body<ListUserFilesResponse.Success>()

  override suspend fun fetchUserFileInfo(
    token: Token,
    budgetId: BudgetId,
  ): GetUserFileInfoResponse.Success =
    client
      .get {
        url {
          protocol = urlProtocol
          host = serverUrl.baseUrl
          path("/sync/get-user-file-info")
        }
        header(AktualHeaders.TOKEN, token)
        header(AktualHeaders.FILE_ID, budgetId)
      }
      .body<GetUserFileInfoResponse.Success>()

  override suspend fun fetchUserKey(body: GetUserKeyRequest): GetUserKeyResponse.Success =
    client
      .post {
        url {
          protocol = urlProtocol
          host = serverUrl.baseUrl
          path("/sync/user-get-key")
        }
        contentType(ContentType.Application.Json)
        setBody(body)
      }
      .body<GetUserKeyResponse.Success>()

  override suspend fun delete(body: DeleteUserFileRequest): DeleteUserFileResponse.Success =
    client
      .post {
        url {
          protocol = urlProtocol
          host = serverUrl.baseUrl
          path("/sync/delete-user-file")
        }
        contentType(ContentType.Application.Json)
        setBody(body)
      }
      .body<DeleteUserFileResponse.Success>()

  /**
   * Bridging between [kotlinx.io.Source] and [okio.Sink], since they don't interoperate out the box
   */
  private fun KxSource.transferTo(sink: OkioSink) {
    val buffer = ByteArray(SINK_BUFFER_SIZE)
    val bufferedSink = sink.buffer()
    while (true) {
      val bytesRead = readAtMostTo(buffer, startIndex = 0, endIndex = buffer.size)
      if (bytesRead <= 0) break
      bufferedSink.write(buffer, offset = 0, byteCount = bytesRead)
    }
    bufferedSink.flush()
  }

  private companion object {
    const val CHANNEL_BUFFER_SIZE = 8192L
    const val SINK_BUFFER_SIZE = 4096
  }
}
