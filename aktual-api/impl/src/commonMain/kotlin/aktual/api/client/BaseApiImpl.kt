package aktual.api.client

import aktual.api.model.base.InfoResponse
import aktual.core.model.ServerUrl
import aktual.di.ServerChosenScope
import dev.zacsweers.metro.ContributesBinding
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

@ContributesBinding(ServerChosenScope::class)
class BaseApiImpl
internal constructor(
  @param:AktualClient private val client: HttpClient,
  private val serverUrl: ServerUrl,
) : BaseApi {
  override suspend fun fetchInfo(): InfoResponse =
    client.get { url(serverUrl, "/info") }.body<InfoResponse>()
}
