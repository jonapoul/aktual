package aktual.budget.rules.vm.edit

import aktual.budget.rules.vm.Rule
import androidx.compose.runtime.Immutable

@Immutable
sealed interface EditRuleState {
  data object Loading : EditRuleState

  @JvmInline value class Failed(val reason: String) : EditRuleState

  data class Loaded(val rule: Rule, val isDeleting: Boolean) : EditRuleState
}
