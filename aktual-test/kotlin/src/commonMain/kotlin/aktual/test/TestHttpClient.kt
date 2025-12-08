package aktual.test

import aktual.api.builder.buildKtorClient
import aktual.api.client.AktualJson
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import kotlinx.serialization.json.Json

fun testHttpClient(
  engine: HttpClientEngine,
  json: Json = AktualJson,
): HttpClient = buildKtorClient(json, tag = null, engine, isDebug = false)
