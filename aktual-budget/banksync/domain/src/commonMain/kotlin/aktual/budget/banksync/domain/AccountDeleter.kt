package aktual.budget.banksync.domain

import aktual.budget.model.AccountId
import aktual.budget.transactions.domain.TransactionWriter
import dev.zacsweers.metro.Inject

/**
 * The forced branch of closeAccount() in packages/loot-core/src/server/accounts/app.ts: unlinks the
 * account from its bank, then deletes it with its transactions.
 */
@Inject
class AccountDeleter(private val linker: BankAccountLinker, private val writer: TransactionWriter) {
  // False if the account was deleted but its GoCardless bank login couldn't be removed
  suspend fun delete(account: AccountId): Boolean {
    val loginRemoved = linker.unlink(account)
    writer.write { deleteAccount(account) }
    return loginRemoved
  }
}
