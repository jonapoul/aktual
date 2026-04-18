package aktual.budget.rules.vm.edit

import androidx.compose.runtime.Immutable

@Immutable
sealed interface EditRuleEvent {
  data object DeletedRule : EditRuleEvent
}
