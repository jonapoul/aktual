package aktual.budget.banksync.domain

import aktual.budget.model.AccountId

interface BankAccountUnlinker {
  /**
   * Clears [account]'s link. Once nothing else uses its GoCardless bank login, that's deleted too.
   * Failing to delete it doesn't stop the unlink, as upstream, but returns false.
   */
  suspend fun unlink(account: AccountId): Boolean
}
