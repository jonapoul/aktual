package aktual.api.client

import aktual.api.model.banksync.EnableBankingAccountType
import aktual.api.model.banksync.EnableBankingAccountsResponse
import aktual.api.model.banksync.EnableBankingBank
import aktual.api.model.banksync.EnableBankingBanksResponse
import aktual.api.model.banksync.EnableBankingLoginResponse
import aktual.api.model.banksync.SecretResponse

/**
 * Logging in to a bank through Enable Banking, which lists accounts only once the user has. Syncing
 * them goes through [BankSyncApi] like any other provider.
 *
 * See packages/sync-server/src/app-enablebanking/app-enablebanking.ts
 */
interface EnableBankingApi {
  suspend fun configure(applicationId: String, secretKey: String): SecretResponse

  suspend fun banks(country: String): EnableBankingBanksResponse

  suspend fun login(
    bank: EnableBankingBank,
    type: EnableBankingAccountType,
  ): EnableBankingLoginResponse

  suspend fun accounts(state: String): EnableBankingAccountsResponse
}
