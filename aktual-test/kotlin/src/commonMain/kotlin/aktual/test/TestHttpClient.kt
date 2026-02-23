package aktual.test

import aktual.api.buildKtorClient
import aktual.core.model.AktualJson
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import kotlinx.serialization.json.Json

fun testHttpClient(engine: HttpClientEngine, json: Json = AktualJson): HttpClient =
  buildKtorClient(json = json, tag = "TEST", engine = engine, isDebug = false)
