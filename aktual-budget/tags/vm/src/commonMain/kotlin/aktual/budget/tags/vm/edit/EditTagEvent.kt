package aktual.budget.tags.vm.edit

import androidx.compose.runtime.Immutable

@Immutable
sealed interface EditTagEvent {
  data object FinishedSaving : EditTagEvent
}
