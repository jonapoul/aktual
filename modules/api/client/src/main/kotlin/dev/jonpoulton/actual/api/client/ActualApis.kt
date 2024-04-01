package dev.jonpoulton.actual.api.client

import dev.jonpoulton.actual.core.model.ServerUrl

data class ActualApis(
  val serverUrl: ServerUrl,
  val account: AccountApi,
  val base: BaseApi,
  val sync: SyncApi,
)
