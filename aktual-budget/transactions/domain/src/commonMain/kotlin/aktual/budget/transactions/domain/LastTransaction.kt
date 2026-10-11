package aktual.budget.transactions.domain

import aktual.di.BudgetScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

// The transaction last added while this budget has been open. As upstream's lastTransaction, the
// next new one starts on its date and in its account
@Inject
@SingleIn(BudgetScope::class)
class LastTransaction {
  private val mutableFields = MutableStateFlow<TransactionFields?>(null)

  val fields: TransactionFields?
    get() = mutableFields.value

  fun set(fields: TransactionFields) = mutableFields.update { fields }
}
