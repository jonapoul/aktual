package aktual.about.vm

import aktual.budget.model.BudgetId

/** Emitted by [ManageStorageViewModel] when a destructive operation requires back stack cleanup. */
sealed interface StorageNavEvent {
  /** All budget files were deleted. Token/prefs still exist. */
  data object AllFilesCleared : StorageNavEvent

  /** A specific budget was deleted and it was the currently-active budget. */
  data class ActiveBudgetCleared(val id: BudgetId) : StorageNavEvent

  /** Preferences (token + serverUrl) were cleared. */
  data object PreferencesCleared : StorageNavEvent
}
