package actual.budget.list.ui

import actual.budget.model.Budget
import androidx.compose.runtime.Immutable
import dev.drewhamilton.poko.Poko

@Immutable
internal sealed interface ListBudgetsAction {
  data object Reload : ListBudgetsAction
  data object ChangePassword : ListBudgetsAction
  data object ChangeServer : ListBudgetsAction
  data object OpenInBrowser : ListBudgetsAction
  data object OpenSettings : ListBudgetsAction
  @Poko class Open(val budget: Budget) : ListBudgetsAction
  @Poko class Delete(val budget: Budget) : ListBudgetsAction
}
