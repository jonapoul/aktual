package aktual.api.client

import aktual.api.model.banksync.BankSyncAccountsResponse
import aktual.api.model.banksync.BankSyncStatusResponse
import aktual.api.model.banksync.BankSyncTransactionsRequest
import aktual.api.model.banksync.BankSyncTransactionsResponse
import aktual.api.model.banksync.GoCardlessAccountsResponse
import aktual.api.model.banksync.GoCardlessBanksResponse
import aktual.api.model.banksync.GoCardlessLoginResponse
import aktual.api.model.banksync.SimpleFinBatchRequest
import aktual.api.model.banksync.SimpleFinBatchResponse
import aktual.budget.model.AccountSyncSource

/**
 * Talks to the sync server's bank sync endpoints, which proxy each provider (GoCardless, SimpleFIN
 * etc.) using credentials held on the server. Nothing here touches the budget: the client decides
 * what to do with downloaded transactions, then sends the result back through the usual budget
 * sync.
 */
interface BankSyncApi {
  /** Whether [source] has credentials set up on the server. */
  suspend fun status(source: AccountSyncSource): BankSyncStatusResponse

  /**
   * Downloads transactions and balances for one linked account from [source]. Gives up after a
   * minute with a [BankSyncTransactionsResponse.ProviderError.TIMED_OUT] error.
   */
  suspend fun transactions(
    source: AccountSyncSource,
    request: BankSyncTransactionsRequest,
  ): BankSyncTransactionsResponse

  /** Downloads several SimpleFIN accounts in one request, giving up after five minutes. */
  suspend fun simpleFinBatch(request: SimpleFinBatchRequest): SimpleFinBatchResponse

  /**
   * Lists the accounts [source] can see at the user's banks, for linking to budget accounts. Only
   * SimpleFIN, Pluggy.ai and Akahu: GoCardless and Enable Banking list them after a bank login
   * instead. Gives up after a minute.
   */
  suspend fun accounts(source: AccountSyncSource): BankSyncAccountsResponse

  /**
   * Lists the banks GoCardless can log in to in [country], an ISO 3166 code. With [showDemo], also
   * GoCardless's sandbox bank for testing.
   */
  suspend fun goCardlessBanks(country: String, showDemo: Boolean = false): GoCardlessBanksResponse

  /**
   * Starts a GoCardless login at [bankId], from [goCardlessBanks]. The user finishes it in a
   * browser, then [goCardlessAccounts] lists what they shared.
   */
  suspend fun goCardlessLogin(bankId: String): GoCardlessLoginResponse

  /** Lists the accounts shared through a GoCardless login, once the user has finished it. */
  suspend fun goCardlessAccounts(requisitionId: String): GoCardlessAccountsResponse

  /**
   * Deletes a GoCardless bank login once no accounts are linked through it. False if GoCardless
   * refused.
   */
  suspend fun removeGoCardlessRequisition(requisitionId: String): Boolean
}
