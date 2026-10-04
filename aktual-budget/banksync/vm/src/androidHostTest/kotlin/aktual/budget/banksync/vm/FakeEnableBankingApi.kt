package aktual.budget.banksync.vm

import aktual.api.client.EnableBankingApi
import aktual.api.model.banksync.EnableBankingAccountType
import aktual.api.model.banksync.EnableBankingAccountsResponse
import aktual.api.model.banksync.EnableBankingBank
import aktual.api.model.banksync.EnableBankingBanksResponse
import aktual.api.model.banksync.EnableBankingLoginResponse
import kotlinx.coroutines.awaitCancellation

internal class FakeEnableBankingApi : EnableBankingApi {
  val banks = mutableMapOf<String, EnableBankingBanksResponse>()
  val bankRequests = mutableListOf<String>()
  var login: EnableBankingLoginResponse? = null
  val logins = mutableListOf<Pair<EnableBankingBank, EnableBankingAccountType>>()
  val polls = ArrayDeque<EnableBankingAccountsResponse>()

  override suspend fun banks(country: String): EnableBankingBanksResponse {
    bankRequests += country
    return banks[country] ?: error("No banks for $country")
  }

  override suspend fun login(
    bank: EnableBankingBank,
    type: EnableBankingAccountType,
  ): EnableBankingLoginResponse {
    logins += bank to type
    return checkNotNull(login)
  }

  // Suspends forever once the queue's empty, as the server's long poll would
  override suspend fun accounts(state: String): EnableBankingAccountsResponse =
    polls.removeFirstOrNull() ?: awaitCancellation()
}
