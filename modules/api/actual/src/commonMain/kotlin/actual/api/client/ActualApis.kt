package actual.api.client

import actual.core.model.ServerUrl
import io.ktor.client.HttpClient
import io.ktor.utils.io.core.Closeable

data class ActualApis(
  val serverUrl: ServerUrl,
  val client: HttpClient,
  val account: AccountApi,
  val base: BaseApi,
  val health: HealthApi,
  val sync: SyncApi,
  val syncDownload: SyncDownloadApi,
) : Closeable by client
