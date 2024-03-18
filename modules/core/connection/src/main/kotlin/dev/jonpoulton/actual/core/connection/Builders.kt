package dev.jonpoulton.actual.core.connection

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dev.jonpoulton.actual.api.client.ActualApis
import dev.jonpoulton.actual.api.json.ActualJson
import dev.jonpoulton.actual.core.model.ServerUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.create
import timber.log.Timber

internal fun buildOkHttp(): OkHttpClient {
  val logger = HttpLoggingInterceptor { Timber.v(it) }
  return OkHttpClient.Builder()
    .addInterceptor(logger)
    .build()
}

internal fun buildRetrofit(client: OkHttpClient, url: ServerUrl): Retrofit {
  val contentType = "application/json".toMediaType()
  return Retrofit.Builder()
    .addConverterFactory(ActualJson.asConverterFactory(contentType))
    .client(client)
    .baseUrl(url.toString())
    .build()
}

internal fun buildApis(retrofit: Retrofit, url: ServerUrl): ActualApis {
  return ActualApis(
    serverUrl = url,
    account = retrofit.create(),
    base = retrofit.create(),
  )
}
