package aktual.api.client

import aktual.api.model.banksync.BankSyncAccountsResponse
import aktual.api.model.banksync.BankSyncStatusResponse
import aktual.api.model.banksync.BankSyncTransactionsRequest
import aktual.api.model.banksync.BankSyncTransactionsResponse.ProviderError
import aktual.api.model.banksync.BankSyncTransactionsResponse.Rejected
import aktual.api.model.banksync.BankSyncTransactionsResponse.Success
import aktual.api.model.banksync.ExternalBankAccount
import aktual.api.model.banksync.GoCardlessAccountsResponse
import aktual.api.model.banksync.GoCardlessBank
import aktual.api.model.banksync.GoCardlessBanksResponse
import aktual.api.model.banksync.GoCardlessLoginResponse
import aktual.api.model.banksync.SimpleFinBatchRequest
import aktual.api.model.banksync.SimpleFinBatchResponse
import aktual.api.model.banksync.SimpleFinBatchResponse.Failed
import aktual.budget.model.Amount
import aktual.budget.model.DbMetadata
import aktual.core.model.AktualJson
import aktual.core.model.BudgetServer
import aktual.test.GocardlessResponses
import aktual.test.PluggyaiResponses
import aktual.test.SimplefinResponses
import aktual.test.TestBudgetLocalPreferences
import aktual.test.emptyMockEngine
import aktual.test.latestRequest
import aktual.test.latestRequestHeaders
import aktual.test.latestRequestUrl
import aktual.test.respondJson
import aktual.test.testHttpClient
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsOnly
import assertk.assertions.doesNotContainKey
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import assertk.assertions.key
import assertk.assertions.prop
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.http.content.TextContent
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

class BankSyncApiTest {
  private lateinit var mockEngine: MockEngine.Queue
  private lateinit var bankSyncApi: BankSyncApi

  @BeforeTest
  fun before() {
    mockEngine = emptyMockEngine()
    bankSyncApi =
      BankSyncApiImpl(
        client = testHttpClient(mockEngine, AktualJson),
        server = BudgetServer.Remote(SERVER_URL, TOKEN),
        prefs = TestBudgetLocalPreferences(DbMetadata(cloudFileId = BUDGET_ID)),
      )
  }

  @AfterTest
  fun after() {
    mockEngine.close()
  }

  @Test
  fun `Status request`() = runTest {
    mockEngine += { respondJson(SimplefinResponses.STATUS_SUCCESS_200) }

    val response = bankSyncApi.status(SimpleFin)

    assertThat(mockEngine.latestRequestUrl()).isEqualTo("https://test.server.com/simplefin/status")
    assertThat(mockEngine.latestRequestHeaders()).all {
      key("X-ACTUAL-TOKEN").containsExactly("abc-123")
      doesNotContainKey("X-ACTUAL-FILE-ID")
    }
    assertThat(response).isEqualTo(BankSyncStatusResponse.Success(configured = true))
  }

  @Test
  fun `Transactions request`() = runTest {
    mockEngine += { respondJson(SimplefinResponses.TRANSACTIONS_SUCCESS_200) }

    bankSyncApi.transactions(
      GoCardless,
      BankSyncTransactionsRequest(
        accountId = "account-1",
        startDate = LocalDate(2026, 7, 1),
        requisitionId = "requisition-1",
        includeBalance = true,
      ),
    )

    assertThat(mockEngine.latestRequestUrl())
      .isEqualTo("https://test.server.com/gocardless/transactions")
    assertThat(mockEngine.latestRequest().body).isInstanceOf<TextContent>().all {
      prop(TextContent::text)
        .isEqualTo(
          """{"accountId":"account-1","startDate":"2026-07-01","requisitionId":"requisition-1","includeBalance":true}""",
        )
    }
  }

  @Test
  fun `Pluggy requests send the file ID`() = runTest {
    mockEngine += { respondJson(SimplefinResponses.TRANSACTIONS_SUCCESS_200) }

    bankSyncApi.transactions(
      PluggyAi,
      BankSyncTransactionsRequest(accountId = "account-1", startDate = LocalDate(2026, 7, 1)),
    )

    assertThat(mockEngine.latestRequestUrl())
      .isEqualTo("https://test.server.com/pluggyai/transactions")
    assertThat(mockEngine.latestRequestHeaders()).all {
      key("X-ACTUAL-TOKEN").containsExactly("abc-123")
      key("X-ACTUAL-FILE-ID").containsExactly("xyz-789")
    }
    assertThat(mockEngine.latestRequest().body).isInstanceOf<TextContent>().all {
      prop(TextContent::text).isEqualTo("""{"accountId":"account-1","startDate":"2026-07-01"}""")
    }
  }

  @Test
  fun `Parse successful transactions`() = runTest {
    mockEngine += { respondJson(SimplefinResponses.TRANSACTIONS_SUCCESS_200) }

    val response = bankSyncApi.transactions(SimpleFin, REQUEST)

    assertThat(response).isInstanceOf<Success>().all {
      prop(Success::startingBalance).isEqualTo(123456L)
      transform { it.balances.map { b -> b.balanceType to b.balanceAmount.amount } }
        .containsExactly("expected" to "1234.56", "interimAvailable" to "1234.56")
      transform { it.transactions.all.map { t -> t.transactionId } }
        .containsExactly("TRN-pending-1", "TRN-booked-1")
      transform { it.transactions.booked.single() }
        .all {
          transform { it.isBooked }.isEqualTo(true)
          transform { it.date }.isEqualTo("2026-09-28")
          transform { it.payeeName }.isEqualTo("Employer Inc")
          transform { it.amount }.isEqualTo("2500.00")
          transform { it["notes"] }.isEqualTo("PAYROLL")
          transform { it["postedDate"] }.isEqualTo("2026-09-28")
        }
      transform { it.transactions.pending.single().isBooked }.isEqualTo(false)
    }
  }

  @Test
  fun `Parse provider error`() = runTest {
    mockEngine += { respondJson(SimplefinResponses.TRANSACTIONS_INVALID_TOKEN_200) }

    val response = bankSyncApi.transactions(SimpleFin, REQUEST)

    assertThat(response)
      .isEqualTo(
        ProviderError(
          errorType = "INVALID_ACCESS_TOKEN",
          errorCode = "INVALID_ACCESS_TOKEN",
          reason =
            "Invalid SimpleFIN access token.  Reset the token and re-link any broken accounts.",
        ),
      )
  }

  @Test
  fun `Parse rate limit error`() = runTest {
    mockEngine += { respondJson(GocardlessResponses.TRANSACTIONS_RATE_LIMIT_200) }

    val response = bankSyncApi.transactions(GoCardless, REQUEST)

    assertThat(response)
      .isEqualTo(
        ProviderError(
          errorType = "RATE_LIMIT_EXCEEDED",
          errorCode = "NORDIGEN_ERROR",
          reason = "Rate limit exceeded",
        ),
      )
  }

  @Test
  fun `Parse rejected request`() = runTest {
    mockEngine += {
      respondJson(PluggyaiResponses.TRANSACTIONS_NOT_CONFIGURED_400, BadRequest)
    }

    val response = bankSyncApi.transactions(PluggyAi, REQUEST)

    assertThat(response)
      .isEqualTo(
        Rejected(
          reason = "not-configured",
          details = "Pluggy credentials are not configured",
        ),
      )
  }

  @Test
  fun `A request that times out is a provider error`() = runTest {
    mockEngine += { throw SocketTimeoutException("Timed out") }
    val response = bankSyncApi.transactions(SimpleFin, REQUEST)
    assertThat(response).isEqualTo(ProviderError(ProviderError.TIMED_OUT, ProviderError.TIMED_OUT))
  }

  @Test
  fun `A batch request that times out fails`() = runTest {
    mockEngine += { throw ConnectTimeoutException("Timed out") }
    val response = bankSyncApi.simpleFinBatch(BATCH_REQUEST)
    assertThat(response)
      .isEqualTo(Failed(ProviderError(ProviderError.TIMED_OUT, ProviderError.TIMED_OUT)))
  }

  @Test
  fun `Batch request`() = runTest {
    mockEngine += { respondJson(SimplefinResponses.TRANSACTIONS_BATCH_200) }

    bankSyncApi.simpleFinBatch(BATCH_REQUEST)

    assertThat(mockEngine.latestRequestUrl())
      .isEqualTo("https://test.server.com/simplefin/transactions")
    assertThat(mockEngine.latestRequest().body).isInstanceOf<TextContent>().all {
      prop(TextContent::text)
        .isEqualTo(
          """{"accountId":["ACT-1","ACT-2","ACT-missing"],"startDate":["2026-07-01","2026-07-02","2026-07-03"]}""",
        )
    }
  }

  @Test
  fun `Parse batch response`() = runTest {
    mockEngine += { respondJson(SimplefinResponses.TRANSACTIONS_BATCH_200) }

    val response = bankSyncApi.simpleFinBatch(BATCH_REQUEST)

    assertThat(response).isInstanceOf<SimpleFinBatchResponse.Success>().all {
      transform { it.accounts.keys }.containsOnly("ACT-1", "ACT-2", "ACT-missing")
      transform { it.accounts }
        .key("ACT-1")
        .isInstanceOf<Success>()
        .all {
          prop(Success::startingBalance).isEqualTo(123456L)
          transform { it.transactions.all.map { t -> t.transactionId } }
            .containsExactly("TRN-booked-1")
        }
      // An account's error wins over its download
      transform { it.accounts }
        .key("ACT-2")
        .isInstanceOf<ProviderError>()
        .prop(ProviderError::errorCode)
        .isEqualTo("ACCOUNT_NEEDS_ATTENTION")
      transform { it.accounts }
        .key("ACT-missing")
        .isInstanceOf<ProviderError>()
        .prop(ProviderError::errorCode)
        .isEqualTo("ACCOUNT_MISSING")
    }
  }

  @Test
  fun `Parse batch provider error`() = runTest {
    mockEngine += { respondJson(SimplefinResponses.TRANSACTIONS_INVALID_TOKEN_200) }

    val response = bankSyncApi.simpleFinBatch(BATCH_REQUEST)

    assertThat(response)
      .isInstanceOf<Failed>()
      .prop(Failed::error)
      .isInstanceOf<ProviderError>()
      .prop(ProviderError::errorCode)
      .isEqualTo("INVALID_ACCESS_TOKEN")
  }

  @Test
  fun `Empty batch response has no data`() = runTest {
    mockEngine += { respondJson("""{"status":"ok","data":{}}""") }

    val response = bankSyncApi.simpleFinBatch(BATCH_REQUEST)

    assertThat(response)
      .isEqualTo(Failed(ProviderError(ProviderError.NO_DATA, ProviderError.NO_DATA)))
  }

  @Test
  fun `Parse SimpleFIN accounts`() = runTest {
    mockEngine += { respondJson(SimplefinResponses.ACCOUNTS_SUCCESS_200) }

    val response = bankSyncApi.accounts(SimpleFin)

    assertThat(mockEngine.latestRequestUrl())
      .isEqualTo("https://test.server.com/simplefin/accounts")
    assertThat(response)
      .isEqualTo(
        BankSyncAccountsResponse.Success(
          listOf(
            ExternalBankAccount(
              accountId = "ACT-1",
              name = "Checking",
              institution = "My Bank",
              orgId = "ORG-1",
              orgDomain = "mybank.example.com",
              balance = Amount(123456),
            ),
            ExternalBankAccount(
              accountId = "ACT-2",
              name = "Credit Card",
              institution = "Other Bank",
              orgId = null,
              orgDomain = null,
              balance = Amount(-5678),
            ),
          ),
        ),
      )
  }

  @Test
  fun `Parse Pluggy accounts`() = runTest {
    mockEngine += { respondJson(PluggyaiResponses.ACCOUNTS_SUCCESS_200) }

    val response = bankSyncApi.accounts(PluggyAi)

    assertThat(mockEngine.latestRequestHeaders()).key("X-ACTUAL-FILE-ID").containsExactly("xyz-789")
    assertThat(response)
      .isEqualTo(
        BankSyncAccountsResponse.Success(
          listOf(
            // Bank accounts are named after their tax number, with invested money in the balance
            ExternalBankAccount(
              accountId = "pluggy-1",
              name = "Conta Corrente - 123.456.789-00",
              institution = "Conta Corrente",
              orgId = "pluggy-1",
              orgDomain = null,
              balance = Amount(12075),
            ),
            ExternalBankAccount(
              accountId = "pluggy-2",
              name = "Cartão - Jane Doe",
              institution = "Cartão",
              orgId = "pluggy-2",
              orgDomain = null,
              balance = Amount(30010),
            ),
          ),
        ),
      )
  }

  @Test
  fun `Parse Akahu accounts`() = runTest {
    mockEngine += {
      respondJson(
        """
        {"status":"ok","data":{"accounts":[{"_id":"acc_1","name":"Everyday",
        "connection":{"_id":"conn_1","name":"ANZ"},"balance":{"current":42.5}}]}}
        """
          .trimIndent(),
      )
    }

    val response = bankSyncApi.accounts(Akahu)

    assertThat(mockEngine.latestRequestUrl()).isEqualTo("https://test.server.com/akahu/accounts")
    assertThat(response)
      .isEqualTo(
        BankSyncAccountsResponse.Success(
          listOf(
            ExternalBankAccount(
              accountId = "acc_1",
              name = "Everyday",
              institution = "ANZ",
              orgId = "conn_1",
              orgDomain = "ANZ",
              balance = Amount(4250),
            ),
          ),
        ),
      )
  }

  @Test
  fun `Parse accounts provider error`() = runTest {
    mockEngine += { respondJson(SimplefinResponses.TRANSACTIONS_INVALID_TOKEN_200) }

    val response = bankSyncApi.accounts(SimpleFin)

    assertThat(response)
      .isInstanceOf<BankSyncAccountsResponse.Failed>()
      .prop(BankSyncAccountsResponse.Failed::error)
      .isInstanceOf<ProviderError>()
      .prop(ProviderError::errorCode)
      .isEqualTo("INVALID_ACCESS_TOKEN")
  }

  @Test
  fun `Parse accounts error message`() = runTest {
    mockEngine += {
      respondJson("""{"status":"ok","data":{"error":"Missing user or app token"}}""")
    }

    val response = bankSyncApi.accounts(Akahu)

    assertThat(response)
      .isEqualTo(
        BankSyncAccountsResponse.Failed(Rejected("Missing user or app token", details = null)),
      )
  }

  @Test
  fun `GoCardless banks request`() = runTest {
    mockEngine += { respondJson(GocardlessResponses.GET_BANKS_SUCCESS_200) }

    val response = bankSyncApi.goCardlessBanks("GB")

    assertThat(mockEngine.latestRequestUrl())
      .isEqualTo("https://test.server.com/gocardless/get-banks")
    assertThat(mockEngine.latestRequest().body)
      .isInstanceOf<TextContent>()
      .prop(TextContent::text)
      .isEqualTo("""{"country":"GB","showDemo":false}""")
    assertThat(response)
      .isEqualTo(
        GoCardlessBanksResponse.Success(
          listOf(
            GoCardlessBank(
              id = "SANDBOXFINANCE_SFIN0000",
              name = "Sandbox Finance",
              logo = "https://cdn.nordigen.com/ais/SANDBOXFINANCE_SFIN0000.png",
            ),
            GoCardlessBank(
              id = "MONZO_MONZGB2L",
              name = "Monzo",
              logo = "https://cdn.nordigen.com/ais/MONZO_MONZGB2L.png",
            ),
          ),
        ),
      )
  }

  @Test
  fun `Parse GoCardless banks error`() = runTest {
    mockEngine += { respondJson(GocardlessResponses.CREATE_WEB_TOKEN_INVALID_ORIGIN_200) }

    val response = bankSyncApi.goCardlessBanks("GB")

    assertThat(response)
      .isEqualTo(
        GoCardlessBanksResponse.Failed(
          ProviderError(errorType = "Invalid Origin header", errorCode = "INTERNAL_ERROR"),
        ),
      )
  }

  @Test
  fun `GoCardless login sends the server as the origin`() = runTest {
    mockEngine += { respondJson(GocardlessResponses.CREATE_WEB_TOKEN_SUCCESS_200) }

    val response = bankSyncApi.goCardlessLogin("SANDBOXFINANCE_SFIN0000")

    assertThat(mockEngine.latestRequestUrl())
      .isEqualTo("https://test.server.com/gocardless/create-web-token")
    assertThat(mockEngine.latestRequestHeaders())
      .key("Origin")
      .containsExactly("https://test.server.com")
    assertThat(mockEngine.latestRequest().body)
      .isInstanceOf<TextContent>()
      .prop(TextContent::text)
      .isEqualTo("""{"institutionId":"SANDBOXFINANCE_SFIN0000","accessValidForDays":90}""")
    assertThat(response)
      .isEqualTo(
        GoCardlessLoginResponse.Success(
          link =
            "https://ob.gocardless.com/ob-psd2/start/3fa85f64-5717-4562-b3fc-2c963f66afa6/" +
              "SANDBOXFINANCE_SFIN0000",
          requisitionId = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        ),
      )
  }

  @Test
  fun `Parse GoCardless login error`() = runTest {
    mockEngine += { respondJson(GocardlessResponses.CREATE_WEB_TOKEN_INVALID_ORIGIN_200) }

    val response = bankSyncApi.goCardlessLogin("SANDBOXFINANCE_SFIN0000")

    assertThat(response)
      .isInstanceOf<GoCardlessLoginResponse.Failed>()
      .prop(GoCardlessLoginResponse.Failed::error)
      .isInstanceOf<ProviderError>()
      .prop(ProviderError::errorType)
      .isEqualTo("Invalid Origin header")
  }

  @Test
  fun `GoCardless accounts are pending until the login finishes`() = runTest {
    mockEngine += { respondJson(GocardlessResponses.GET_ACCOUNTS_PENDING_200) }

    val response = bankSyncApi.goCardlessAccounts(REQUISITION)

    assertThat(mockEngine.latestRequestUrl())
      .isEqualTo("https://test.server.com/gocardless/get-accounts")
    assertThat(mockEngine.latestRequest().body)
      .isInstanceOf<TextContent>()
      .prop(TextContent::text)
      .isEqualTo("""{"requisitionId":"$REQUISITION"}""")
    assertThat(response).isEqualTo(Pending)
  }

  @Test
  fun `Parse GoCardless accounts`() = runTest {
    mockEngine += { respondJson(GocardlessResponses.GET_ACCOUNTS_SUCCESS_200) }

    val response = bankSyncApi.goCardlessAccounts(REQUISITION)

    assertThat(response)
      .isEqualTo(
        GoCardlessAccountsResponse.Success(
          listOf(
            ExternalBankAccount(
              accountId = "7e57ac6b-0f61-4a2d-9a0d-1c2b3d4e5f60",
              name = "Main Account (XXX 4321) EUR",
              institution = "Sandbox Finance",
              orgId = REQUISITION,
              orgDomain = null,
              balance = null,
            ),
            ExternalBankAccount(
              accountId = "1a2b3c4d-5e6f-4a7b-8c9d-0e1f2a3b4c5d",
              name = "Savings",
              institution = "Sandbox Finance",
              orgId = REQUISITION,
              orgDomain = null,
              balance = null,
            ),
          ),
        ),
      )
  }

  @Test
  fun `Parse GoCardless accounts error`() = runTest {
    mockEngine += { respondJson(GocardlessResponses.TRANSACTIONS_RATE_LIMIT_200) }

    val response = bankSyncApi.goCardlessAccounts(REQUISITION)

    assertThat(response).isInstanceOf<GoCardlessAccountsResponse.Failed>()
  }

  @Test
  fun `Remove GoCardless requisition`() = runTest {
    mockEngine += { respondJson("""{"status":"ok","data":{"summary":"Requisition deleted"}}""") }

    val removed = bankSyncApi.removeGoCardlessRequisition("req-1")

    assertThat(removed).isTrue()
    assertThat(mockEngine.latestRequestUrl())
      .isEqualTo("https://test.server.com/gocardless/remove-account")
    assertThat(mockEngine.latestRequest().body).isInstanceOf<TextContent>().all {
      prop(TextContent::text).isEqualTo("""{"requisitionId":"req-1"}""")
    }
  }

  @Test
  fun `Failed GoCardless requisition removal`() = runTest {
    mockEngine += {
      respondJson("""{"status":"error","data":{"reason":"Can not delete requisition"}}""")
    }

    assertThat(bankSyncApi.removeGoCardlessRequisition("req-1")).isFalse()
  }

  private companion object {
    val BATCH_REQUEST =
      SimpleFinBatchRequest(
        accountIds = listOf("ACT-1", "ACT-2", "ACT-missing"),
        startDates = listOf(LocalDate(2026, 7, 1), LocalDate(2026, 7, 2), LocalDate(2026, 7, 3)),
      )
    const val REQUISITION = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
    val REQUEST = BankSyncTransactionsRequest(accountId = "abc", startDate = LocalDate(2026, 7, 1))
  }
}
