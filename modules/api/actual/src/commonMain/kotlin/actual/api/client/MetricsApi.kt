package actual.api.client

import actual.api.model.metrics.GetMetricsResponse
import actual.codegen.GET
import actual.codegen.KtorApi
import actual.core.model.ServerUrl
import io.ktor.client.HttpClient

@KtorApi
interface MetricsApi {
  @GET("/metrics")
  suspend fun getMetrics(): GetMetricsResponse
}

expect fun MetricsApi(serverUrl: ServerUrl, client: HttpClient): MetricsApi
