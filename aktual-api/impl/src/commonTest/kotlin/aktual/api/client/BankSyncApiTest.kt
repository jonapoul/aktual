package aktual.api.client

import aktual.api.model.banksync.BankSyncStatusResponse
import aktual.api.model.banksync.BankSyncTransactionsRequest
import aktual.api.model.banksync.BankSyncTransactionsResponse
import aktual.api.model.banksync.BankSyncTransactionsResponse.ProviderError
import aktual.api.model.banksync.SimpleFinBatchRequest
import aktual.api.model.banksync.SimpleFinBatchResponse
import aktual.budget.model.AccountSyncSource
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
import assertk.assertions.isInstanceOf
import assertk.assertions.key
import assertk.assertions.prop
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.HttpStatusCode
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

    val response = bankSyncApi.status(AccountSyncSource.SimpleFin)

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
      AccountSyncSource.GoCardless,
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
          """{"accountId":"account-1","startDate":"2026-07-01","requisitionId":"requisition-1","includeBalance":true}"""
        )
    }
  }

  @Test
  fun `Pluggy requests send the file ID`() = runTest {
    mockEngine += { respondJson(SimplefinResponses.TRANSACTIONS_SUCCESS_200) }

    bankSyncApi.transactions(
      AccountSyncSource.PluggyAi,
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

    val response = bankSyncApi.transactions(AccountSyncSource.SimpleFin, REQUEST)

    assertThat(response).isInstanceOf<BankSyncTransactionsResponse.Success>().all {
      prop(BankSyncTransactionsResponse.Success::startingBalance).isEqualTo(123456L)
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

    val response = bankSyncApi.transactions(AccountSyncSource.SimpleFin, REQUEST)

    assertThat(response)
      .isEqualTo(
        BankSyncTransactionsResponse.ProviderError(
          errorType = "INVALID_ACCESS_TOKEN",
          errorCode = "INVALID_ACCESS_TOKEN",
          reason =
            "Invalid SimpleFIN access token.  Reset the token and re-link any broken accounts.",
        )
      )
  }

  @Test
  fun `Parse rate limit error`() = runTest {
    mockEngine += { respondJson(GocardlessResponses.TRANSACTIONS_RATE_LIMIT_200) }

    val response = bankSyncApi.transactions(AccountSyncSource.GoCardless, REQUEST)

    assertThat(response)
      .isEqualTo(
        BankSyncTransactionsResponse.ProviderError(
          errorType = "RATE_LIMIT_EXCEEDED",
          errorCode = "NORDIGEN_ERROR",
          reason = "Rate limit exceeded",
        )
      )
  }

  @Test
  fun `Parse rejected request`() = runTest {
    mockEngine += {
      respondJson(PluggyaiResponses.TRANSACTIONS_NOT_CONFIGURED_400, HttpStatusCode.BadRequest)
    }

    val response = bankSyncApi.transactions(AccountSyncSource.PluggyAi, REQUEST)

    assertThat(response)
      .isEqualTo(
        BankSyncTransactionsResponse.Rejected(
          reason = "not-configured",
          details = "Pluggy credentials are not configured",
        )
      )
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
          """{"accountId":["ACT-1","ACT-2","ACT-missing"],"startDate":["2026-07-01","2026-07-02","2026-07-03"]}"""
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
        .isInstanceOf<BankSyncTransactionsResponse.Success>()
        .all {
          prop(BankSyncTransactionsResponse.Success::startingBalance).isEqualTo(123456L)
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
      .isInstanceOf<SimpleFinBatchResponse.Failed>()
      .prop(SimpleFinBatchResponse.Failed::error)
      .isInstanceOf<ProviderError>()
      .prop(ProviderError::errorCode)
      .isEqualTo("INVALID_ACCESS_TOKEN")
  }

  @Test
  fun `Empty batch response has no data`() = runTest {
    mockEngine += { respondJson("""{"status":"ok","data":{}}""") }

    val response = bankSyncApi.simpleFinBatch(BATCH_REQUEST)

    assertThat(response)
      .isEqualTo(
        SimpleFinBatchResponse.Failed(ProviderError(ProviderError.NO_DATA, ProviderError.NO_DATA))
      )
  }

  private companion object {
    val BATCH_REQUEST =
      SimpleFinBatchRequest(
        accountIds = listOf("ACT-1", "ACT-2", "ACT-missing"),
        startDates = listOf(LocalDate(2026, 7, 1), LocalDate(2026, 7, 2), LocalDate(2026, 7, 3)),
      )
    val REQUEST = BankSyncTransactionsRequest(accountId = "abc", startDate = LocalDate(2026, 7, 1))
  }
}
