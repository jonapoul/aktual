package actual.core.connection

import actual.api.client.ActualApis
import actual.api.json.ActualJson
import actual.url.model.ServerUrl
import alakazam.kotlin.core.ifTrue
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import timber.log.Timber

internal fun buildOkHttp(isDebug: Boolean): OkHttpClient {
  val logger = HttpLoggingInterceptor { Timber.tag("ACTUAL HTTP").v(it) }
  logger.setLevel(HttpLoggingInterceptor.Level.BODY)
  return OkHttpClient
    .Builder()
    .ifTrue(isDebug) { addInterceptor(logger) }
    .build()
}

internal fun buildRetrofit(client: OkHttpClient, url: ServerUrl): Retrofit {
  val contentType = "application/json".toMediaType()
  return Retrofit
    .Builder()
    .addConverterFactory(ActualJson.asConverterFactory(contentType))
    .client(client)
    .baseUrl(url.toString())
    .build()
}

internal fun buildApis(retrofit: Retrofit, url: ServerUrl) = ActualApis(
  serverUrl = url,
  account = retrofit.create(),
  base = retrofit.create(),
)
