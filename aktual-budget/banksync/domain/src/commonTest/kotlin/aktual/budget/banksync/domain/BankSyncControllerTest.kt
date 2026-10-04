package aktual.budget.banksync.domain

import aktual.api.model.banksync.BankSyncTransactionsRequest
import aktual.api.model.banksync.BankSyncTransactionsResponse
import aktual.api.model.banksync.SimpleFinBatchResponse
import aktual.budget.banksync.domain.BankSyncError.Provider
import aktual.budget.banksync.domain.BankSyncError.Rejected
import aktual.budget.banksync.domain.BankSyncResult.Failed
import aktual.budget.banksync.domain.BankSyncResult.Synced
import aktual.budget.db.Accounts
import aktual.budget.model.AccountId
import aktual.budget.model.AccountSyncSource
import aktual.budget.model.Amount
import aktual.budget.model.BankSyncStatus
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.containsOnly
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.isTrue
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonNull

internal class BankSyncControllerTest {
  private val api = FakeBankSyncApi()

  @Test
  fun `Only open accounts with a bank and provider ID sync, off budget ones last`() =
    runBankSyncTest {
      insertLinkedAccount(A, offBudget = true)
      insertLinkedAccount(B)
      insertLinkedAccount(C, accountId = "")
      insertLinkedAccount(D)
      database.accountsQueries.closeAccount(D)
      api.responses += mapOf("provider-a" to success(), "provider-b" to success())

      val results = controller(api).sync()

      assertThat(results.map { it.account }).containsExactly(B, A)
    }

  @Test
  fun `Only the given accounts sync`() = runBankSyncTest {
    insertLinkedAccount(A)
    insertLinkedAccount(B)
    api.responses["provider-b"] = success()

    val results = controller(api).sync(setOf(B))

    assertThat(results.map { it.account }).containsExactly(B)
  }

  @Test
  fun `Requests carry what each provider needs`() = runBankSyncTest {
    insertLinkedAccount(A, source = GoCardless, bankId = "requisition-1")
    insertLinkedAccount(B, source = EnableBanking, bankName = "ASPSP")
    insertLinkedAccount(C, source = Akahu)
    api.responses += listOf("a", "b", "c").associate { "provider-$it" to success() }

    controller(api).sync()

    assertThat(api.requests)
      .containsExactlyInAnyOrder(
        AccountSyncSource.GoCardless to
          BankSyncTransactionsRequest(
            accountId = "provider-a",
            startDate = START,
            requisitionId = "requisition-1",
            includeBalance = true,
          ),
        AccountSyncSource.EnableBanking to
          BankSyncTransactionsRequest("provider-b", START, aspspName = "ASPSP"),
        AccountSyncSource.Akahu to BankSyncTransactionsRequest("provider-c", START),
      )
  }

  @Test
  fun `Later syncs start from the oldest transaction, at most 90 days ago`() = runBankSyncTest {
    insertLinkedAccount(A)
    insertLinkedAccount(B)
    api.responses["provider-a"] =
      success(bankTx("-1.00", date = "2026-09-30", transactionId = "t1"))
    api.responses["provider-b"] =
      success(bankTx("-1.00", date = "2026-01-01", transactionId = "t2"))
    val controller = controller(api)

    controller.sync()
    api.requests.clear()
    controller.sync()

    assertThat(api.requests.map { it.second })
      .containsExactlyInAnyOrder(
        BankSyncTransactionsRequest(
          accountId = "provider-a",
          startDate = LocalDate(2026, 9, 30),
          requisitionId = "bank-a",
          includeBalance = false,
        ),
        BankSyncTransactionsRequest(
          accountId = "provider-b",
          startDate = START,
          requisitionId = "bank-b",
          includeBalance = false,
        ),
      )
  }

  @Test
  fun `A sync imports the download and records when it happened`() = runBankSyncTest {
    insertLinkedAccount(A)
    api.responses["provider-a"] = success(bankTx("-12.34", transactionId = "t1"), balance = 10_000)
    val controller = controller(api)

    val first = controller.sync()

    assertThat(first.single()).isInstanceOf<Synced>().all {
      // With the starting balance
      prop(Synced::added).hasSize(2)
      prop(Synced::updated).isEmpty()
    }
    assertThat(liveIds(A)).hasSize(2)
    assertThat(account(A)).all {
      prop(Accounts::last_sync).isEqualTo(NOW)
      prop(Accounts::bank_sync_status).isEqualTo(Ok)
    }

    api.responses["provider-a"] = success(bankTx("-12.34", transactionId = "t1"), balance = 9_000)
    val second = controller.sync()

    assertThat(second.single()).isInstanceOf<Synced>().all {
      prop(Synced::added).isEmpty()
      // Nothing about it changed
      prop(Synced::updated).isEmpty()
    }
    assertThat(liveIds(A)).hasSize(2)
    assertThat(account(A).balance_current).isEqualTo(Amount(9_000))
  }

  @Test
  fun `Provider errors set the account's status`() = runBankSyncTest {
    insertLinkedAccount(A)
    api.responses["provider-a"] = providerError("ITEM_ERROR", "ITEM_LOGIN_REQUIRED")

    val results = controller(api).sync()

    assertThat(results)
      .containsExactly(
        Failed(
          account = A,
          name = "a",
          error = Provider("ITEM_ERROR", "ITEM_LOGIN_REQUIRED"),
        )
      )
    assertThat(account(A)).all {
      prop(Accounts::last_sync).isNull()
      prop(Accounts::bank_sync_status).isEqualTo(ReauthRequired)
    }
  }

  @Test
  fun `Rejected requests fail`() = runBankSyncTest {
    insertLinkedAccount(A)
    api.responses["provider-a"] =
      BankSyncTransactionsResponse.Rejected(reason = "not-configured", details = null)

    val results = controller(api).sync()

    assertThat(results.single())
      .isInstanceOf<Failed>()
      .prop(Failed::error)
      .isEqualTo(Rejected("not-configured"))
    assertThat(account(A).bank_sync_status).isEqualTo(BankSyncStatus.Failed)
  }

  @Test
  fun `Accounts without a provider fail without a request`() = runBankSyncTest {
    insertLinkedAccount(A, source = null)

    val results = controller(api).sync()

    assertThat(results.single()).isInstanceOf<Failed>()
    assertThat(api.requests).isEmpty()
    assertThat(account(A).bank_sync_status).isEqualTo(BankSyncStatus.Failed)
  }

  @Test
  fun `A download that can't be imported fails`() = runBankSyncTest {
    insertLinkedAccount(A)
    // No date
    api.responses["provider-a"] = success(bankTx("-1.00") { put("date", JsonNull) })

    val results = controller(api).sync()

    assertThat(results.single())
      .isInstanceOf<Failed>()
      .prop(Failed::error)
      .isInstanceOf<BankSyncError.Internal>()
    assertThat(liveIds(A)).isEmpty()
    assertThat(account(A).bank_sync_status).isEqualTo(BankSyncStatus.Failed)
  }

  @Test
  fun `Failed requests don't stop other accounts syncing`() = runBankSyncTest {
    insertLinkedAccount(A)
    api.error = IllegalStateException("Offline")

    val results = controller(api).sync()

    assertThat(results).containsExactly(Failed(A, "a", BankSyncError.Internal("Offline")))
  }

  @Test
  fun `Failed writes don't stop other accounts syncing`() = runBankSyncTest {
    insertLinkedAccount(A)
    insertLinkedAccount(B, offBudget = true)
    api.responses += mapOf("provider-a" to success(), "provider-b" to providerError("TIMED_OUT"))
    syncError = IllegalStateException("Disk full")

    val results = controller(api).sync()

    assertThat(results)
      .containsExactly(
        Failed(A, "a", BankSyncError.Internal("Disk full")),
        Failed(B, "b", Provider("TIMED_OUT", "TIMED_OUT")),
      )
  }

  @Test
  fun `SimpleFIN accounts download together`() = runBankSyncTest {
    insertLinkedAccount(A, source = SimpleFin)
    insertLinkedAccount(B, source = SimpleFin)
    insertLinkedAccount(C, source = SimpleFin)
    insertLinkedAccount(D, source = GoCardless)
    api.batchResponse =
      SimpleFinBatchResponse.Success(
        mapOf(
          "provider-a" to success(bankTx("-1.00", transactionId = "t1"), balance = 500),
          "provider-b" to providerError("ACCOUNT_NEEDS_ATTENTION"),
        )
      )
    api.responses["provider-d"] = success()

    val results = controller(api).sync()

    assertThat(api.batchRequests.single()).all {
      transform { it.accountIds.zip(it.startDates) }
        .containsExactlyInAnyOrder(
          "provider-a" to START,
          "provider-b" to START,
          "provider-c" to START,
        )
    }
    assertThat(api.requests.map { it.second.accountId }).containsExactly("provider-d")
    assertThat(results.associate { it.account to it::class })
      .containsOnly(
        A to Synced::class,
        B to Failed::class,
        C to Failed::class,
        D to Synced::class,
      )
    assertThat(liveIds(A)).hasSize(2)
    assertThat(account(A).bank_sync_status).isEqualTo(Ok)
    assertThat(account(B).bank_sync_status).isEqualTo(AttentionRequired)
    // Not in the response at all
    assertThat(account(C).bank_sync_status).isEqualTo(AccountMissing)
  }

  @Test
  fun `A failed SimpleFIN batch fails each account`() = runBankSyncTest {
    insertLinkedAccount(A, source = SimpleFin)
    insertLinkedAccount(B, source = SimpleFin)
    api.batchResponse = SimpleFinBatchResponse.Failed(providerError("INVALID_ACCESS_TOKEN"))

    val results = controller(api).sync()

    assertThat(results.map { it.account }).containsExactlyInAnyOrder(A, B)
    assertThat(account(A).bank_sync_status).isEqualTo(ReauthRequired)
    assertThat(account(B).bank_sync_status).isEqualTo(ReauthRequired)
  }

  @Test
  fun `One SimpleFIN account downloads on its own`() = runBankSyncTest {
    insertLinkedAccount(A, source = SimpleFin)
    api.responses["provider-a"] = success()

    controller(api).sync()

    assertThat(api.batchRequests).isEmpty()
    assertThat(api.requests)
      .containsExactly(
        AccountSyncSource.SimpleFin to BankSyncTransactionsRequest("provider-a", START)
      )
  }

  @Test
  fun `Progress follows a sync started in the background`() = runBankSyncTest {
    insertLinkedAccount(A)
    insertLinkedAccount(B, offBudget = true)
    api.responses += mapOf("provider-a" to success(), "provider-b" to providerError("TIMED_OUT"))
    val controller = controller(api)

    controller.progress.test {
      assertThat(awaitItem()).isEqualTo(BankSyncProgress())
      assertThat(controller.start()).isTrue()
      // Already running
      assertThat(controller.start()).isFalse()

      assertThat(awaitItem()).isEqualTo(BankSyncProgress(isRunning = true, pending = listOf(A, B)))
      var progress = awaitItem()
      while (progress.isRunning) progress = awaitItem()

      assertThat(progress.pending).isEmpty()
      assertThat(progress.results.map { it.account }).containsExactly(A, B)
    }
    assertThat(controller.start()).isTrue()
  }

  @Test
  fun `Error statuses`() {
    val cases =
      mapOf(
        Provider("ITEM_ERROR", "ITEM_LOGIN_REQUIRED") to ReauthRequired,
        Provider("INVALID_INPUT", "INVALID_ACCESS_TOKEN") to ReauthRequired,
        Provider("INVALID_ACCESS_TOKEN", "X") to ReauthRequired,
        Provider("ITEM_ERROR", "X") to BankSyncStatus.Failed,
        Provider("ACCOUNT_NEEDS_ATTENTION", "X") to AttentionRequired,
        Provider("RATE_LIMIT_EXCEEDED", "X") to RateLimitExceeded,
        Provider("TIMED_OUT", "X") to TimedOut,
        Provider("ACCOUNT_MISSING", "X") to AccountMissing,
        Provider("NORDIGEN_ERROR", "X") to BankSyncStatus.Failed,
        Rejected("not-configured") to BankSyncStatus.Failed,
        BankSyncError.Internal("Broken") to BankSyncStatus.Failed,
      )

    assertThat(cases.mapValues { (error, _) -> error.status }).isEqualTo(cases)
  }

  private companion object {
    val A = AccountId("a")
    val B = AccountId("b")
    val C = AccountId("c")
    val D = AccountId("d")

    // 89 days before TODAY, which with today makes 90
    val START = LocalDate(2026, 7, 6)
  }
}
