package aktual.budget.rules.ui.list

import aktual.budget.model.RuleId
import aktual.budget.rules.vm.Rule
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableSet

@Immutable internal sealed interface ListRulesAction

internal data object Reload : ListRulesAction

internal data object CreateNew : ListRulesAction

@JvmInline internal value class Open(val id: RuleId) : ListRulesAction

@JvmInline internal value class Delete(val id: RuleId) : ListRulesAction

@JvmInline internal value class DeleteMultiple(val ids: ImmutableSet<RuleId>) : ListRulesAction

@JvmInline internal value class Edit(val rule: Rule) : ListRulesAction

@JvmInline internal value class OpenUrl(val url: String) : ListRulesAction

internal data object EnableCheckboxes : ListRulesAction

internal data object DisableCheckboxes : ListRulesAction

@JvmInline internal value class Check(val id: RuleId) : ListRulesAction

@JvmInline internal value class Uncheck(val id: RuleId) : ListRulesAction

internal data object UncheckAll : ListRulesAction
