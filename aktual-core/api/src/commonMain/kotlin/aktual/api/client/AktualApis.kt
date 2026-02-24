package aktual.api.client

import aktual.core.model.ServerUrl

data class AktualApis(
  val serverUrl: ServerUrl,
  val account: AccountApi,
  val base: BaseApi,
  val health: HealthApi,
  val metrics: MetricsApi,
  val sync: SyncApi,
)
