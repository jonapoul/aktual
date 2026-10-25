package aktual.budget.rules.ui.search

import aktual.budget.model.RuleId
import androidx.compose.runtime.Immutable

internal sealed interface SearchRulesAction

internal data object NavBack : SearchRulesAction

internal data object Reload : SearchRulesAction

@JvmInline internal value class SetQuery(val query: String) : SearchRulesAction

@JvmInline internal value class OpenRule(val id: RuleId) : SearchRulesAction

@Immutable
internal fun interface SearchRulesActionHandler {
  operator fun invoke(action: SearchRulesAction)
}
