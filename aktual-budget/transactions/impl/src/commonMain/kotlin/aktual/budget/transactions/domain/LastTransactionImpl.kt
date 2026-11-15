package aktual.budget.transactions.domain

import aktual.di.BudgetScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

@Inject
@SingleIn(BudgetScope::class)
@ContributesBinding(BudgetScope::class)
class LastTransactionImpl : LastTransaction {
  private val mutableFields = MutableStateFlow<TransactionFields?>(null)

  override val fields: TransactionFields?
    get() = mutableFields.value

  override fun set(fields: TransactionFields) = mutableFields.update { fields }
}
