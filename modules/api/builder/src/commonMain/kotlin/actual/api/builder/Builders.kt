package actual.api.builder

import actual.log.Logger
import actual.url.model.ServerUrl
import alakazam.kotlin.core.ifTrue
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

fun buildOkHttp(
  isDebug: Boolean,
  tag: String,
) = OkHttpClient
  .Builder()
  .ifTrue(isDebug) { addLogger(tag) }
  .build()

fun buildRetrofit(
  client: OkHttpClient,
  url: ServerUrl,
  json: Json,
): Retrofit {
  val contentType = "application/json".toMediaType()
  return Retrofit
    .Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .addConverterFactory(json.asConverterFactory(contentType))
    .client(client)
    .baseUrl(url.toString())
    .build()
}

private fun OkHttpClient.Builder.addLogger(tag: String): OkHttpClient.Builder {
  val interceptor = HttpLoggingInterceptor { Logger.tag(tag).v(it) }
  interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
  return addInterceptor(interceptor)
}
