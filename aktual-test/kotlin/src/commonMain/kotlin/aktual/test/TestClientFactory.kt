package aktual.test

import aktual.api.builder.ClientFactory
import aktual.api.builder.buildKtorClient
import io.ktor.client.engine.mock.MockEngine
import kotlinx.serialization.json.Json

class TestClientFactory(private val mockEngine: MockEngine) : ClientFactory {
  override fun invoke(json: Json) = buildKtorClient(json, isDebug = false, tag = "TEST", engine = mockEngine)
}
