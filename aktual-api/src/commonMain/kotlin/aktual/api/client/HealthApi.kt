package aktual.api.client

import aktual.api.model.health.GetHealthResponse

interface HealthApi {
  suspend fun getHealth(): GetHealthResponse
}
