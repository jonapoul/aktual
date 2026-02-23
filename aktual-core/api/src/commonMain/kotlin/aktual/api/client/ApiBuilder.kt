package aktual.api.client

import aktual.core.model.ServerUrl

interface ApiBuilder {
  fun account(): AccountApi

  fun base(): BaseApi

  fun health(): HealthApi

  fun metrics(): MetricsApi

  fun sync(): SyncApi

  fun interface Factory {
    fun create(url: ServerUrl): ApiBuilder
  }
}
