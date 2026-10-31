package aktual.budget.banksync.domain

import aktual.budget.model.AccountId
import aktual.budget.transactions.domain.TransactionWriter
import dev.zacsweers.metro.Inject
import kotlin.coroutines.cancellation.CancellationException
import logcat.logcat

/**
 * The forced branch of closeAccount() in packages/loot-core/src/server/accounts/app.ts: deletes the
 * account with its transactions, then unlinks it from its bank. Upstream unlinks first, but then a
 * failed delete would leave the account unlinked and its bank login possibly gone.
 */
@Inject
class AccountDeleter
internal constructor(private val linker: BankAccountLinker, private val writer: TransactionWriter) {
  // False if the account was deleted but couldn't be unlinked, or its GoCardless bank login
  // couldn't be removed
  suspend fun delete(account: AccountId): Boolean {
    writer.write { deleteAccount(account) }
    return try {
      linker.unlink(account)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      logcat.w(e) { "Deleted $account but failed unlinking it" }
      false
    }
  }
}
