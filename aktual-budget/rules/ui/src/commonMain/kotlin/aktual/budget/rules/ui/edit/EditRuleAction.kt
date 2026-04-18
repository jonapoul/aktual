package aktual.budget.rules.ui.edit

import aktual.budget.model.RuleId
import androidx.compose.runtime.Immutable

@Immutable internal interface EditRuleAction

internal data object NavBack : EditRuleAction

internal data class Delete(val id: RuleId) : EditRuleAction

@Immutable
internal fun interface EditRuleActionHandler {
  operator fun invoke(action: EditRuleAction)
}
