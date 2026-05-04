package aktual.budget.list.vm

import androidx.compose.runtime.Immutable

@Immutable
sealed interface ListBudgetsEvent {
  data object NavToBudget : ListBudgetsEvent
}
