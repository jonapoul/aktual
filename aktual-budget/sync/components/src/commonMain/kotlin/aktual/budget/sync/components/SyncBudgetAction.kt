package aktual.budget.sync.components

import aktual.core.model.Password

internal sealed interface SyncBudgetAction

internal data object Retry : SyncBudgetAction

internal data class EnterKeyPassword(val input: Password) : SyncBudgetAction

internal data object ConfirmKeyPassword : SyncBudgetAction

internal data object LearnMore : SyncBudgetAction

internal data object Cancel : SyncBudgetAction
