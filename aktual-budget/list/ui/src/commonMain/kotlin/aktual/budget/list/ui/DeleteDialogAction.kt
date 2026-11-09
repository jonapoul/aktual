package aktual.budget.list.ui

import androidx.compose.runtime.Immutable

@Immutable internal sealed interface DeleteDialogAction

internal data object DeleteLocal : DeleteDialogAction

internal data object DeleteRemote : DeleteDialogAction

internal data object Dismiss : DeleteDialogAction
