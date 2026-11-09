package aktual.budget.rules.ui.list

import aktual.budget.model.RuleId

internal sealed interface ListRulesAction

internal data object Retry : ListRulesAction

internal data object CreateNew : ListRulesAction

internal data object OpenSearch : ListRulesAction

@JvmInline internal value class Delete(val id: RuleId) : ListRulesAction

@JvmInline internal value class Edit(val id: RuleId) : ListRulesAction

@JvmInline internal value class OpenUrl(val url: String) : ListRulesAction
