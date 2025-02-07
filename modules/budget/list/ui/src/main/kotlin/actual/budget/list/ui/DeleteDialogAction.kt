package actual.budget.list.ui

import androidx.compose.runtime.Immutable

@Immutable
sealed interface DeleteDialogAction {
  data object DeleteLocal : DeleteDialogAction
  data object DeleteRemote : DeleteDialogAction
  data object Dismiss : DeleteDialogAction
}
