package actual.api.client

import actual.api.model.health.GetHealthResponse
import actual.codegen.GET
import actual.codegen.KtorApi
import actual.core.model.ServerUrl
import io.ktor.client.HttpClient

@KtorApi
interface HealthApi {
  @GET("/health")
  suspend fun getHealth(): GetHealthResponse
}

expect fun HealthApi(serverUrl: ServerUrl, client: HttpClient): HealthApi
