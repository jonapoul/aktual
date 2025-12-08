package aktual.budget.transactions.ui

import aktual.budget.model.TransactionId
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate

@Immutable
internal interface StateSource {
  fun isChecked(id: TransactionId): Flow<Boolean>
  fun isExpanded(date: LocalDate): Flow<Boolean>

  object Empty : StateSource {
    override fun isChecked(id: TransactionId): Flow<Boolean> = flowOf()
    override fun isExpanded(date: LocalDate): Flow<Boolean> = flowOf()
  }
}
