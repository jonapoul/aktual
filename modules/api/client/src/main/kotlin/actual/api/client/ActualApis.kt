package actual.api.client

import actual.core.model.ServerUrl

data class ActualApis(
  val serverUrl: ServerUrl,
  val account: AccountApi,
  val base: BaseApi,
)
