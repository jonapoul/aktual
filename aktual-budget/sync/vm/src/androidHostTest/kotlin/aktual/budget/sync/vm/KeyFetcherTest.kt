package aktual.budget.sync.vm

import aktual.api.client.AktualApisStateHolder
import aktual.api.client.AktualJson
import aktual.api.client.SyncApi
import aktual.budget.encryption.BufferDecrypter
import aktual.budget.encryption.BufferDecrypterImpl
import aktual.budget.model.BudgetId
import aktual.core.model.LoginToken
import aktual.core.model.Password
import aktual.core.model.Protocol
import aktual.core.model.ServerUrl
import aktual.core.model.base64
import aktual.prefs.EncryptedPreferences
import aktual.prefs.KeyPreferences
import aktual.test.buildPreferences
import aktual.test.emptyMockEngine
import aktual.test.respondJson
import aktual.test.testHttpClient
import alakazam.test.core.TestCoroutineContexts
import assertk.assertThat
import assertk.assertions.isEqualTo
import dev.jonpoulton.preferences.core.Preferences
import io.ktor.client.engine.mock.MockEngine
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
class KeyFetcherTest {
  private lateinit var keyFetcher: KeyFetcher
  private lateinit var keyPreferences: KeyPreferences
  private lateinit var decrypter: BufferDecrypter

  private lateinit var mockEngine: MockEngine.Queue
  private lateinit var syncApi: SyncApi

  @BeforeTest
  fun before() {
    val contexts = TestCoroutineContexts(EmptyCoroutineContext)
    val prefs = buildPreferences(EmptyCoroutineContext)
    val encryptedPrefs = object : EncryptedPreferences, Preferences by prefs {}
    keyPreferences = KeyPreferences(encryptedPrefs)
    decrypter = BufferDecrypterImpl(contexts, keyPreferences)

    mockEngine = emptyMockEngine()
    syncApi = SyncApi(SERVER_URL, testHttpClient(mockEngine, AktualJson))
    val stateHolder = AktualApisStateHolder()
    stateHolder.value = mockk(relaxed = true) { every { sync } returns syncApi }

    keyFetcher = KeyFetcher(
      stateHolder = stateHolder,
      contexts = contexts,
      keyPreferences = keyPreferences,
      decrypter = decrypter,
    )
  }

  @AfterTest
  fun after() {
    mockEngine.close()
  }

  @Test
  fun `Fetch and test successfully`() = runTest {
    // given
    mockEngine += { respondJson(VALID_RESPONSE) }

    // when
    val result = keyFetcher(BUDGET_ID, TOKEN, CORRECT_PASSWORD)

    // then
    assertThat(result)
      .isEqualTo(FetchKeyResult.Success("KQWSxKj5Gc27pPk127N7QkIFxNJtopx0doU4O5pSWd0=".base64()))
  }

  private companion object {
    val BUDGET_ID = BudgetId("abc-123")
    val TOKEN = LoginToken(value = "xyz-789")
    val SERVER_URL = ServerUrl(Protocol.Https, "test.server.com")

    val CORRECT_PASSWORD = Password("P@ssw0rd!")

    val VALID_RESPONSE = """
      {
        "status": "ok",
        "data": {
          "id": "a57fc3bf-fa98-44f2-82b7-54b4755703a9",
          "salt": "1EqpueIJKQLC8Sbyfr0mH3Am+wyB0J6TTteJg/Yux0I=",
          "test": "{\"value\":\"CSoGSJ0+CX+sTq4xkaOBIVXoALreifDvbZ9Vwdx5R/yZPwwfoaMViDn/DH6J4mDuebTD5XF13z9PbzMw4hR4DJFD3OJKKtSDen0ozNfGxhQRELkGKLcsWMxzhjWDM1LQjRi9PUi2mEA=\",\"meta\":{\"keyId\":\"a57fc3bf-fa98-44f2-82b7-54b4755703a9\",\"algorithm\":\"aes-256-gcm\",\"iv\":\"dzrtFWZRScdJy6xq\",\"authTag\":\"hJodLzlWpyS4w9ZSn/iyfQ==\"}}"
        }
      }
    """.trimIndent()
  }
}
