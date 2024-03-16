package dev.jonpoulton.actual.api.client

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dev.jonpoulton.actual.api.json.ActualJson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.create
import timber.log.Timber

fun buildOkHttp(): OkHttpClient {
  val logger = HttpLoggingInterceptor { Timber.v(it) }
  return OkHttpClient.Builder()
    .addInterceptor(logger)
    .build()
}

fun buildRetrofit(client: OkHttpClient, baseUrl: String): Retrofit {
  val contentType = "application/json".toMediaType()
  return Retrofit.Builder()
    .addConverterFactory(ActualJson.asConverterFactory(contentType))
    .client(client)
    .baseUrl(baseUrl)
    .build()
}

fun buildApis(retrofit: Retrofit): ActualApis {
  return ActualApis(
    serverUrl = retrofit.baseUrl().toString(),
    account = retrofit.create(),
    base = retrofit.create(),
  )
}
