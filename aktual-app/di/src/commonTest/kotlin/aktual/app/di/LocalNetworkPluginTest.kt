package aktual.app.di

import aktual.core.model.ServerUrl
import aktual.core.utils.LocalNetworkBlockedException
import aktual.core.utils.LocalNetworkPermission
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.messageContains
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.request.get
import kotlin.test.Test
import kotlinx.coroutines.async
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okio.IOException

class LocalNetworkPluginTest {
  private val permission = FakeLocalNetworkPermission()
  private val prompt = LocalNetworkPromptImpl()

  private fun client(engine: MockEngine) =
    HttpClient(engine) { install(localNetworkPlugin(permission, prompt, strings = { "Blocked" })) }

  @Test
  fun `Hold requests while the prompt is showing`() = runTest {
    val engine = MockEngine { respondOk() }
    val client = client(engine)

    // Given
    prompt.onShowing()

    // When
    val response = async { client.get("http://192.168.1.5/") }
    advanceUntilIdle()

    // Then it waits for the answer before sending
    assertThat(engine.requestHistory).isEmpty()
    prompt.onAnswered()
    response.await()
    assertThat(engine.requestHistory).hasSize(1)
  }

  @Test
  fun `Map failures to blocked exception when the permission is missing`() = runTest {
    val client = client(MockEngine { throw IOException("Timed out") })
    permission.granted = false
    permission.isRequired = true

    assertFailure { client.get("http://[fd00::5]:5006/") }
      .isInstanceOf<LocalNetworkBlockedException>()
      .messageContains("Blocked")
    assertThat(permission.checkedHost).isEqualTo("fd00::5")
  }

  @Test
  fun `Keep failures as they are when the permission isn't the problem`() = runTest {
    val client = client(MockEngine { throw IOException("Timed out") })
    permission.granted = true
    permission.isRequired = true

    assertFailure { client.get("http://192.168.1.5/") }.messageContains("Timed out")
  }

  private class FakeLocalNetworkPermission : LocalNetworkPermission {
    var granted = true
    var isRequired = false
    var checkedHost: String? = null

    override fun isGranted(): Boolean = granted

    override suspend fun isRequiredFor(url: ServerUrl): Boolean = isRequired

    override suspend fun isRequiredForHost(host: String): Boolean {
      checkedHost = host
      return isRequired
    }
  }
}
