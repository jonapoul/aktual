package actual.budget.transactions.ui

import actual.budget.model.TransactionId
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate

@Immutable
internal interface TransactionCheckbox {
  fun isChecked(id: TransactionId): Flow<Boolean>
  fun onCheckedChange(id: TransactionId, isChecked: Boolean)
}

@Immutable
internal interface TransactionHeader {
  fun isExpanded(date: LocalDate): Flow<Boolean>
  fun onExpandedChange(date: LocalDate, isExpanded: Boolean)
}

internal object PreviewTransactionCheckbox : TransactionCheckbox {
  override fun isChecked(id: TransactionId): Flow<Boolean> = flowOf()
  override fun onCheckedChange(id: TransactionId, isChecked: Boolean) = Unit
}

internal object PreviewTransactionHeader : TransactionHeader {
  override fun isExpanded(date: LocalDate): Flow<Boolean> = flowOf()
  override fun onExpandedChange(date: LocalDate, isExpanded: Boolean) = Unit
}
