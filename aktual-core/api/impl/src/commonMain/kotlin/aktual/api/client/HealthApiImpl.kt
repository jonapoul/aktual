package aktual.api.client

import aktual.api.model.health.GetHealthResponse
import aktual.core.model.ServerUrl
import aktual.di.ServerChosenScope
import dev.zacsweers.metro.ContributesBinding
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.path

@ContributesBinding(ServerChosenScope::class)
class HealthApiImpl(
  @param:AktualClient private val client: HttpClient,
  private val serverUrl: ServerUrl,
) : HealthApi {
  private val urlProtocol = serverUrl.protocol()

  override suspend fun getHealth(): GetHealthResponse =
    client
      .get {
        url {
          protocol = urlProtocol
          host = serverUrl.baseUrl
          path("/health")
        }
      }
      .body<GetHealthResponse>()
}
