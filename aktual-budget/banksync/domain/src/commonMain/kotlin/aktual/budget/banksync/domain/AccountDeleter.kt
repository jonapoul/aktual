package aktual.budget.banksync.domain

import aktual.budget.model.AccountId
import aktual.budget.transactions.domain.TransactionWriter
import dev.zacsweers.metro.Inject

/**
 * The forced branch of closeAccount() in packages/loot-core/src/server/accounts/app.ts: deletes the
 * account with its transactions, then unlinks it from its bank. Upstream unlinks first, but then a
 * failed delete would leave the account unlinked and its bank login possibly gone.
 */
@Inject
class AccountDeleter(private val linker: BankAccountLinker, private val writer: TransactionWriter) {
  // False if the account was deleted but its GoCardless bank login couldn't be removed
  suspend fun delete(account: AccountId): Boolean {
    writer.write { deleteAccount(account) }
    return linker.unlink(account)
  }
}
