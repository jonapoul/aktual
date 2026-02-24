package aktual.test

import aktual.api.buildKtorClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import kotlinx.serialization.json.Json

fun testHttpClient(engine: HttpClientEngine, json: Json = PrettyJson): HttpClient =
  buildKtorClient(json = json, tag = "TEST", engine = engine, isDebug = false)
