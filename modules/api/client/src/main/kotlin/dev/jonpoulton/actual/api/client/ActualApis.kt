package dev.jonpoulton.actual.api.client

data class ActualApis(
  val serverUrl: String,
  val account: AccountApi,
  val base: BaseApi,
)
