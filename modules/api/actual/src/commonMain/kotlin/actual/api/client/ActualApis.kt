package actual.api.client

import actual.core.model.ServerUrl
import dev.drewhamilton.poko.Poko
import io.ktor.client.HttpClient
import io.ktor.utils.io.core.Closeable

@Poko class ActualApis(
  val serverUrl: ServerUrl,
  val client: HttpClient,
  val account: AccountApi,
  val base: BaseApi,
  val sync: SyncApi,
  val syncDownload: SyncDownloadApi,
) : Closeable by client
