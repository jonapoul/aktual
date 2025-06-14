package actual.test

import actual.api.builder.buildKtorClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import kotlinx.serialization.json.Json

fun testHttpClient(
  engine: HttpClientEngine,
  json: Json,
): HttpClient = buildKtorClient(json, tag = null, engine, isDebug = false)
