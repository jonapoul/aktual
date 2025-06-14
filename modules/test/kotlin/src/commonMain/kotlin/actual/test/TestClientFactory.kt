package actual.test

import actual.api.builder.ClientFactory
import actual.api.builder.buildKtorClient
import io.ktor.client.engine.mock.MockEngine
import kotlinx.serialization.json.Json

class TestClientFactory(private val mockEngine: MockEngine) : ClientFactory {
  override fun build(json: Json) = buildKtorClient(json, isDebug = false, tag = "TEST", engine = mockEngine)
}
