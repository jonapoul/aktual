package aktual.api.client

import aktual.api.model.banksync.BankSyncTransactionsResponse.ProviderError
import aktual.api.model.banksync.BankSyncTransactionsResponse.Rejected
import aktual.api.model.banksync.EnableBankingAccountType.Business
import aktual.api.model.banksync.EnableBankingAccountsResponse
import aktual.api.model.banksync.EnableBankingBank
import aktual.api.model.banksync.EnableBankingBanksResponse
import aktual.api.model.banksync.EnableBankingLoginResponse
import aktual.api.model.banksync.ExternalBankAccount
import aktual.budget.model.Amount
import aktual.core.model.AktualJson
import aktual.core.model.BudgetServer
import aktual.test.EnablebankingResponses
import aktual.test.emptyMockEngine
import aktual.test.latestRequest
import aktual.test.latestRequestUrl
import aktual.test.respondJson
import aktual.test.testHttpClient
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.content.TextContent
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class EnableBankingApiTest {
  private lateinit var mockEngine: MockEngine.Queue
  private lateinit var api: EnableBankingApi

  @BeforeTest
  fun before() {
    mockEngine = emptyMockEngine()
    api =
      EnableBankingApiImpl(
        client = testHttpClient(mockEngine, AktualJson),
        server = BudgetServer.Remote(SERVER_URL, TOKEN),
      )
  }

  @AfterTest
  fun after() {
    mockEngine.close()
  }

  @Test
  fun `Banks request`() = runTest {
    mockEngine += { respondJson(EnablebankingResponses.ASPSPS_SUCCESS_200) }

    val response = api.banks("FI")

    assertThat(mockEngine.latestRequestUrl())
      .isEqualTo("https://test.server.com/enablebanking/aspsps")
    assertThat(mockEngine.latestRequest().body)
      .isInstanceOf<TextContent>()
      .prop(TextContent::text)
      .isEqualTo("""{"country":"FI"}""")
    assertThat(response)
      .isEqualTo(
        EnableBankingBanksResponse.Success(
          listOf(
            EnableBankingBank(
              name = "Mock ASPSP",
              country = "FI",
              logo = "https://enablebanking.com/brands/FI/Mock%20ASPSP/",
              isBeta = false,
              maxConsentValidity = 7776000,
            ),
            EnableBankingBank(
              name = "Nordea",
              country = "FI",
              logo = "https://enablebanking.com/brands/FI/Nordea/",
              isBeta = true,
              maxConsentValidity = 15552000,
            ),
          )
        )
      )
  }

  @Test
  fun `Parse banks error message`() = runTest {
    mockEngine += { respondJson(EnablebankingResponses.ASPSPS_UNAUTHORIZED_200) }

    val response = api.banks("FI")

    assertThat(response)
      .isEqualTo(
        EnableBankingBanksResponse.Failed(
          Rejected("Enable Banking API error: 401 Unauthorized", details = null)
        )
      )
  }

  @Test
  fun `Login redirects to the server`() = runTest {
    mockEngine += { respondJson(EnablebankingResponses.START_AUTH_SUCCESS_200) }

    val response = api.login(BANK, Business)

    assertThat(mockEngine.latestRequestUrl())
      .isEqualTo("https://test.server.com/enablebanking/start-auth")
    assertThat(mockEngine.latestRequest().body)
      .isInstanceOf<TextContent>()
      .prop(TextContent::text)
      .isEqualTo(
        """{"aspsp":{"name":"Mock ASPSP","country":"FI"},""" +
          """"redirectUrl":"https://test.server.com/enablebanking/auth_callback",""" +
          """"maxConsentValidity":7776000,"psuType":"business"}"""
      )
    assertThat(response)
      .isEqualTo(
        EnableBankingLoginResponse.Success(
          url =
            "https://tilisy.enablebanking.com/welcome?" +
              "sessionid=73100c65-c54d-46a1-87d1-aa3effde435a",
          state = STATE,
        )
      )
  }

  @Test
  fun `Parse login error`() = runTest {
    mockEngine += { respondJson(EnablebankingResponses.START_AUTH_INVALID_INPUT_200) }

    val response = api.login(BANK, Business)

    assertThat(response)
      .isEqualTo(
        EnableBankingLoginResponse.Failed(
          ProviderError(
            errorType = "aspsp, redirectUrl and psuType are required",
            errorCode = "INVALID_INPUT",
          )
        )
      )
  }

  @Test
  fun `Parse accounts`() = runTest {
    mockEngine += { respondJson(EnablebankingResponses.POLL_AUTH_SUCCESS_200) }

    val response = api.accounts(STATE)

    assertThat(mockEngine.latestRequestUrl())
      .isEqualTo("https://test.server.com/enablebanking/poll-auth")
    assertThat(mockEngine.latestRequest().body)
      .isInstanceOf<TextContent>()
      .prop(TextContent::text)
      .isEqualTo("""{"state":"$STATE"}""")
    assertThat(response)
      .isEqualTo(
        EnableBankingAccountsResponse.Success(
          listOf(
            ExternalBankAccount(
              accountId = "07cc67f4-45d6-494b-adac-09b5cbc7e2b5",
              name = "Current Account",
              institution = "Mock ASPSP",
              orgId = "07cc67f4-45d6-494b-adac-09b5cbc7e2b5",
              orgDomain = null,
              balance = Amount(123456L),
            ),
            ExternalBankAccount(
              accountId = "9b5d1a3c-2e4f-4b6a-8c7d-1e2f3a4b5c6d",
              name = "FI4950009420028730",
              institution = "Mock ASPSP",
              orgId = "9b5d1a3c-2e4f-4b6a-8c7d-1e2f3a4b5c6d",
              orgDomain = null,
              balance = Amount(-2050L),
            ),
          )
        )
      )
  }

  @Test
  fun `Accounts time out`() = runTest {
    mockEngine += { respondJson(EnablebankingResponses.POLL_AUTH_TIMEOUT_200) }

    val response = api.accounts(STATE)

    assertThat(response)
      .isEqualTo(
        EnableBankingAccountsResponse.Failed(
          ProviderError(ProviderError.TIMED_OUT, ProviderError.TIMED_OUT)
        )
      )
  }

  private companion object {
    val BANK = EnableBankingBank(name = "Mock ASPSP", country = "FI", maxConsentValidity = 7776000)
    const val STATE = "5f0c3b4e-7a3f-4f3e-9c1d-2b8e6a1d4c7f"
  }
}
