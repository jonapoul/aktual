package aktual.budget.list.ui

import aktual.budget.model.Budget
import androidx.compose.runtime.Immutable

@Immutable internal sealed interface ListBudgetsAction

internal data object Reload : ListBudgetsAction

internal data object ChangePassword : ListBudgetsAction

internal data object LogOut : ListBudgetsAction

internal data object OpenAbout : ListBudgetsAction

internal data object OpenInBrowser : ListBudgetsAction

internal data object OpenSettings : ListBudgetsAction

internal data object OpenServerMetrics : ListBudgetsAction

@JvmInline internal value class Open(val budget: Budget) : ListBudgetsAction

@JvmInline internal value class Delete(val budget: Budget) : ListBudgetsAction

@Immutable
internal fun interface ListBudgetsActionHandler {
  operator fun invoke(action: ListBudgetsAction)
}
