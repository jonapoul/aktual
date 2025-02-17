package actual.core.connection

import actual.api.client.ActualApis
import actual.api.client.ActualJson
import actual.api.core.buildOkHttp
import actual.api.core.buildRetrofit
import actual.log.Logger
import actual.url.model.ServerUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.create

internal fun buildOkHttp(
  logger: Logger,
  isDebug: Boolean,
) = buildOkHttp(logger, isDebug, tag = "ACTUAL HTTP")

internal fun buildRetrofit(
  client: OkHttpClient,
  url: ServerUrl,
) = buildRetrofit(client, url, ActualJson)

internal fun buildApis(
  retrofit: Retrofit,
  url: ServerUrl,
) = ActualApis(
  serverUrl = url,
  account = retrofit.create(),
  base = retrofit.create(),
  sync = retrofit.create(),
)
