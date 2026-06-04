package aktual.budget.sync.ui

import aktual.core.model.Password
import androidx.compose.runtime.Immutable

@Immutable internal sealed interface SyncBudgetAction

internal data object Retry : SyncBudgetAction

internal data object Continue : SyncBudgetAction

internal data class EnterKeyPassword(val input: Password) : SyncBudgetAction

internal data object ConfirmKeyPassword : SyncBudgetAction

internal data object LearnMore : SyncBudgetAction

internal data object Cancel : SyncBudgetAction

@Immutable
internal fun interface SyncBudgetActionHandler {
  operator fun invoke(action: SyncBudgetAction)
}
