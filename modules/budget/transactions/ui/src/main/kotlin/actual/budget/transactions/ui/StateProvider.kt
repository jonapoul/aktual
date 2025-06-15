package actual.budget.transactions.ui

import actual.budget.model.TransactionId
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate

@Immutable
internal interface StateProvider {
  fun isChecked(id: TransactionId): Flow<Boolean>
  fun isExpanded(date: LocalDate): Flow<Boolean>

  object Empty : StateProvider {
    override fun isChecked(id: TransactionId): Flow<Boolean> = flowOf()
    override fun isExpanded(date: LocalDate): Flow<Boolean> = flowOf()
  }
}
