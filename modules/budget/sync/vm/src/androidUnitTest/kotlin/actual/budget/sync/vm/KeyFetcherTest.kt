package actual.budget.sync.vm

import actual.core.model.LoginToken
import actual.core.model.Password
import actual.api.client.ActualApisStateHolder
import actual.api.client.ActualJson
import actual.api.client.SyncApi
import actual.budget.encryption.KeyGenerator
import actual.budget.model.BudgetId
import actual.core.model.Protocol
import actual.core.model.ServerUrl
import actual.core.model.base64
import actual.prefs.EncryptedPreferences
import actual.prefs.KeyPreferences
import actual.test.buildPreferences
import actual.test.emptyMockEngine
import actual.test.plusAssign
import actual.test.respondJson
import actual.test.testHttpClient
import alakazam.test.core.TestCoroutineContexts
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
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class KeyFetcherTest {
  private lateinit var keyFetcher: KeyFetcher
  private lateinit var keyGenerator: KeyGenerator
  private lateinit var keyPreferences: KeyPreferences
  private lateinit var decrypter: Decrypter

  private lateinit var mockEngine: MockEngine
  private lateinit var syncApi: SyncApi

  @BeforeTest
  fun before() {
    val contexts = TestCoroutineContexts(EmptyCoroutineContext)
    keyGenerator = KeyGenerator()

    val prefs = buildPreferences(EmptyCoroutineContext)
    val encryptedPrefs = object : EncryptedPreferences, Preferences by prefs {}

    keyPreferences = KeyPreferences(encryptedPrefs)
    decrypter = Decrypter(contexts, keyPreferences, files = mockk())

    mockEngine = emptyMockEngine()
    syncApi = SyncApi(SERVER_URL, testHttpClient(mockEngine, ActualJson))
    val stateHolder = ActualApisStateHolder()
    stateHolder.value = mockk(relaxed = true) { every { sync } returns syncApi }

    keyFetcher = KeyFetcher(
      stateHolder = stateHolder,
      contexts = contexts,
      keyGenerator = keyGenerator,
      keyPreferences = keyPreferences,
      decrypter = decrypter,
    )
  }

  @AfterTest
  fun after() {
    mockEngine.close()
  }

  @Ignore("Key generation doesn't work at the mo. See https://github.com/jonapoul/actual-android/issues/208")
  @Test
  fun `Fetch and test successfully`() = runTest {
    // given
    mockEngine += { respondJson(VALID_RESPONSE) }

    // when
    val result = keyFetcher(BUDGET_ID, TOKEN, CORRECT_PASSWORD)

    // then
    assertEquals(
      expected = FetchKeyResult.Success("KQWSxKj5Gc27pPk127N7QkIFxNJtopx0doU4O5pSWd0=".base64),
      actual = result,
    )
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
