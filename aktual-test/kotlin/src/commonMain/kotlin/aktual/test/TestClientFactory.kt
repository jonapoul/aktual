package aktual.test

import aktual.core.api.ClientFactory
import aktual.core.api.buildKtorClient
import io.ktor.client.engine.mock.MockEngine
import kotlinx.serialization.json.Json

class TestClientFactory(private val mockEngine: MockEngine) : ClientFactory {
  override fun invoke(json: Json) =
    buildKtorClient(json = json, isDebug = false, tag = "TEST", engine = mockEngine)
}
