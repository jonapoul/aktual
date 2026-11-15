package aktual.budget.banksync.domain

import aktual.budget.model.AccountId
import aktual.budget.transactions.domain.TransactionWriter
import aktual.core.utils.runCatchingCancellable
import aktual.di.BudgetScope
import aktual.log
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(BudgetScope::class)
class AccountDeleterImpl(
  private val linker: BankAccountLinker,
  private val writer: TransactionWriter,
) : AccountDeleter {
  override suspend fun delete(account: AccountId): Boolean {
    writer.write { deleteAccount(account) }
    return runCatchingCancellable(
      attempt = { linker.unlink(account) },
      onFailure = { e ->
        log.w(e) { "Deleted $account but failed unlinking it" }
        false
      },
    )
  }
}
