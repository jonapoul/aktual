package aktual.budget.transactions.vm

import aktual.budget.banksync.domain.NewTransactions
import aktual.budget.banksync.domain.NewTransactionsImpl
import aktual.budget.model.AccountId
import aktual.budget.model.TransactionId
import aktual.di.BudgetScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

@Inject
@SingleIn(BudgetScope::class)
@ContributesBinding(BudgetScope::class, replaces = [NewTransactionsImpl::class])
internal class FakeNewTransactions : NewTransactions {
  override val counts = MutableStateFlow<Map<AccountId, Int>>(emptyMap())
  val seen = mutableListOf<AccountId>()

  override suspend fun imported(account: AccountId, ids: List<TransactionId>) = error("Not used")

  override suspend fun markSeen(account: AccountId) {
    seen += account
    counts.update { it - account }
  }
}
