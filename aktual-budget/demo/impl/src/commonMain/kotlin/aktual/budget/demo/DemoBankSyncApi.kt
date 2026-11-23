package aktual.budget.demo

import aktual.api.client.BankSyncApi
import aktual.api.model.banksync.BankSyncAccountsResponse
import aktual.api.model.banksync.BankSyncStatusResponse
import aktual.api.model.banksync.BankSyncTransaction
import aktual.api.model.banksync.BankSyncTransactionsRequest
import aktual.api.model.banksync.BankSyncTransactionsResponse
import aktual.api.model.banksync.GoCardlessAccountsResponse
import aktual.api.model.banksync.GoCardlessBanksResponse
import aktual.api.model.banksync.GoCardlessLoginResponse
import aktual.api.model.banksync.SimpleFinBatchRequest
import aktual.api.model.banksync.SimpleFinBatchResponse
import aktual.budget.model.AccountSyncSource
import aktual.core.utils.Calendar
import aktual.core.utils.UuidGenerator
import dev.zacsweers.metro.Inject
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/**
 * Stands in for the sync server in the demo budget, which has none. Every provider counts as set
 * up, and each download makes up a few new payments, so syncing always has something to show.
 * Linking needs a real bank login, so that's refused.
 */
@Inject
class DemoBankSyncApi
internal constructor(
  private val calendar: Calendar,
  private val uuidGenerator: UuidGenerator,
  private val random: Random,
) : BankSyncApi {
  override suspend fun status(source: AccountSyncSource): BankSyncStatusResponse {
    pause(QuickResponse)
    return BankSyncStatusResponse.Success(configured = true)
  }

  override suspend fun transactions(
    source: AccountSyncSource,
    request: BankSyncTransactionsRequest,
  ): BankSyncTransactionsResponse {
    pause(SlowResponse)
    return download(request.startDate)
  }

  override suspend fun simpleFinBatch(request: SimpleFinBatchRequest): SimpleFinBatchResponse {
    pause(SlowResponse)
    return SimpleFinBatchResponse.Success(
      request.accountIds.zip(request.startDates).associate { (id, start) -> id to download(start) },
    )
  }

  override suspend fun accounts(source: AccountSyncSource): BankSyncAccountsResponse {
    pause(QuickResponse)
    return BankSyncAccountsResponse.Failed(Unavailable)
  }

  override suspend fun goCardlessBanks(country: String, showDemo: Boolean) =
    GoCardlessBanksResponse.Failed(Unavailable).also { pause(QuickResponse) }

  override suspend fun goCardlessLogin(bankId: String) =
    GoCardlessLoginResponse.Failed(Unavailable).also { pause(QuickResponse) }

  override suspend fun goCardlessAccounts(requisitionId: String) =
    GoCardlessAccountsResponse.Failed(Unavailable).also { pause(QuickResponse) }

  override suspend fun removeGoCardlessRequisition(requisitionId: String): Boolean {
    pause(QuickResponse)
    return false
  }

  // Like a real server, so the loading and syncing states show
  private suspend fun pause(millis: IntRange) {
    delay(random.nextInt(millis.first, millis.last + 1).milliseconds)
  }

  private fun download(startDate: LocalDate): BankSyncTransactionsResponse.Success {
    val today = calendar.today()
    val transactions =
      List(random.nextInt(from = 1, until = MAX_PER_SYNC + 1)) {
        val daysAgo = random.nextInt(until = MAX_DAYS_AGO + 1)
        transaction(date = maxOf(startDate, today.minus(daysAgo, DAY)))
      }
    return BankSyncTransactionsResponse.Success(
      BankSyncTransactionsResponse.Success.Transactions(all = transactions, booked = transactions),
    )
  }

  private fun transaction(date: LocalDate): BankSyncTransaction {
    val (payee, range) = Payments.random(random)
    val cents = random.nextInt(range.first, range.last + 1)
    val json = buildJsonObject {
      put("transactionId", uuidGenerator())
      put("booked", true)
      put("date", date.toString())
      put("payeeName", payee)
      putJsonObject("transactionAmount") {
        put("amount", formatAmount(-cents))
        put("currency", "USD")
      }
    }
    return BankSyncTransaction(json)
  }

  private fun formatAmount(cents: Int): String {
    val sign = if (cents < 0) "-" else ""
    val abs = cents.absoluteValue
    return "$sign${abs / CENTS}.${(abs % CENTS).toString().padStart(length = 2, padChar = '0')}"
  }

  private companion object {
    const val MAX_PER_SYNC = 3
    const val MAX_DAYS_AGO = 2
    const val CENTS = 100
    val QuickResponse = 300..1_000
    val SlowResponse = 1_000..3_000

    val Unavailable =
      BankSyncTransactionsResponse.Rejected(
        reason = "Not available in the demo budget",
        details = null,
      )

    // Payees from the demo budget, so the rules give them their usual categories. Ranges in cents
    val Payments =
      listOf(
        "Kroger" to 1500..12000,
        "Publix" to 1500..9000,
        "Home Depot" to 1000..25000,
        "Movies" to 1200..4000,
        "Online store" to 1000..15000,
      )
  }
}
