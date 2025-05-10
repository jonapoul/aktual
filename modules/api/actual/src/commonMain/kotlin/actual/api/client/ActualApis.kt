package actual.api.client

import actual.url.model.ServerUrl
import io.ktor.client.HttpClient
import io.ktor.utils.io.core.Closeable

data class ActualApis(
  val serverUrl: ServerUrl,
  val client: HttpClient,
  val account: AccountApi,
  val base: BaseApi,
  val sync: SyncApi,
) : Closeable by client
