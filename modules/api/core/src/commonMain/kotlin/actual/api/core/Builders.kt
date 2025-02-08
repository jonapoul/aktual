package actual.api.core

import actual.log.Logger
import actual.url.model.ServerUrl
import alakazam.kotlin.core.ifTrue
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

fun buildOkHttp(
  logger: Logger,
  isDebug: Boolean,
  tag: String,
) = OkHttpClient
  .Builder()
  .ifTrue(isDebug) { addInterceptor(logger, tag) }
  .build()

private fun OkHttpClient.Builder.addInterceptor(
  logger: Logger,
  tag: String,
): OkHttpClient.Builder {
  val interceptor = HttpLoggingInterceptor { logger.tag(tag).v(it) }
  interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
  return addInterceptor(interceptor)
}

fun buildRetrofit(
  client: OkHttpClient,
  url: ServerUrl,
  json: Json,
): Retrofit {
  val contentType = "application/json".toMediaType()
  return Retrofit
    .Builder()
    .addConverterFactory(json.asConverterFactory(contentType))
    .client(client)
    .baseUrl(url.toString())
    .build()
}
