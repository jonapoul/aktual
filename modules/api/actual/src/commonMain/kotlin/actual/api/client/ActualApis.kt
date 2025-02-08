package actual.api.client

import actual.url.model.ServerUrl

data class ActualApis(
  val serverUrl: ServerUrl,
  val account: AccountApi,
  val base: BaseApi,
)
