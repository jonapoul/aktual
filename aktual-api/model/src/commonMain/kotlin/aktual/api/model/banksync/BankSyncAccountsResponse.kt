package aktual.api.model.banksync

import aktual.budget.model.Amount

/**
 * An account at a bank, as a provider lists it for linking to a budget account. Normalised from
 * each provider's own shape, as useBuiltInBankSyncProviders.ts does.
 *
 * @property accountId The provider's ID for the account, stored as `accounts.account_id`.
 * @property orgId With [orgDomain], identifies the bank in `banks.bank_id`.
 * @property balance The current balance, if the provider sent one.
 */
data class ExternalBankAccount(
  val accountId: String,
  val name: String,
  val institution: String?,
  val orgId: String?,
  val orgDomain: String?,
  val balance: Amount?,
)

sealed interface BankSyncAccountsResponse {
  data class Success(val accounts: List<ExternalBankAccount>) : BankSyncAccountsResponse

  data class Failed(val error: BankSyncTransactionsResponse.Failure) : BankSyncAccountsResponse
}
