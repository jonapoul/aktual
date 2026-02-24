package aktual.api.client

import aktual.api.model.metrics.GetMetricsResponse

interface MetricsApi {
  suspend fun getMetrics(): GetMetricsResponse
}
