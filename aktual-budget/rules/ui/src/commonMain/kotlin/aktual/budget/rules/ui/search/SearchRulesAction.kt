package aktual.budget.rules.ui.search

import aktual.budget.model.RuleId

internal sealed interface SearchRulesAction

internal data object NavBack : SearchRulesAction

internal data object Reload : SearchRulesAction

internal data object ClearPayee : SearchRulesAction

@JvmInline internal value class SetQuery(val query: String) : SearchRulesAction

@JvmInline internal value class OpenRule(val id: RuleId) : SearchRulesAction
