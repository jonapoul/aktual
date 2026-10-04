package aktual.api.client

import aktual.api.model.banksync.BankSyncTransactionsResponse
import aktual.api.model.banksync.EnableBankingAccountType
import aktual.api.model.banksync.EnableBankingAccountsResponse
import aktual.api.model.banksync.EnableBankingBank
import aktual.api.model.banksync.EnableBankingBanksResponse
import aktual.api.model.banksync.EnableBankingLoginResponse

/**
 * Logging in to a bank through Enable Banking, which lists accounts only once the user has. Syncing
 * them goes through [BankSyncApi] like any other provider.
 *
 * See packages/sync-server/src/app-enablebanking/app-enablebanking.ts
 */
interface EnableBankingApi {
  /** Lists the banks Enable Banking can log in to in [country], an ISO 3166 code. */
  suspend fun banks(country: String): EnableBankingBanksResponse

  /**
   * Starts a login at [bank], from [banks]. The user finishes it in a browser, while [accounts]
   * waits for what they shared.
   */
  suspend fun login(
    bank: EnableBankingBank,
    type: EnableBankingAccountType,
  ): EnableBankingLoginResponse

  /**
   * Waits for the user to finish the login started with [state], then lists the accounts they
   * shared. The server gives up after five minutes, failing with a
   * [BankSyncTransactionsResponse.ProviderError.TIMED_OUT] error.
   */
  suspend fun accounts(state: String): EnableBankingAccountsResponse
}
