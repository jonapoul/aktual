package aktual.budget.rules.ui.list

import aktual.budget.model.RuleId
import aktual.budget.rules.vm.RuleListItem
import androidx.compose.runtime.Immutable

@Immutable internal sealed interface ListRulesAction

internal data object Reload : ListRulesAction

@JvmInline internal value class Open(val id: RuleId) : ListRulesAction

@JvmInline internal value class Delete(val id: RuleId) : ListRulesAction

@JvmInline internal value class Edit(val rule: RuleListItem) : ListRulesAction

@JvmInline internal value class OpenUrl(val url: String) : ListRulesAction

internal data object EnableCheckboxes : ListRulesAction

internal data object DisableCheckboxes : ListRulesAction

@JvmInline internal value class Check(val id: RuleId) : ListRulesAction

@JvmInline internal value class Uncheck(val id: RuleId) : ListRulesAction
