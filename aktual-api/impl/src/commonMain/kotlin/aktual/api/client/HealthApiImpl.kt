package aktual.api.client

import aktual.api.model.health.GetHealthResponse
import aktual.core.model.ServerUrl
import aktual.di.ServerChosenScope
import dev.zacsweers.metro.ContributesBinding
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

@ContributesBinding(ServerChosenScope::class)
class HealthApiImpl
internal constructor(
  @param:AktualClient private val client: HttpClient,
  private val serverUrl: ServerUrl,
) : HealthApi {
  override suspend fun getHealth(): GetHealthResponse =
    client.get { url(serverUrl, "/health") }.body<GetHealthResponse>()
}
