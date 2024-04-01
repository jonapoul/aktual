package dev.jonpoulton.actual.listbudgets.vm

import androidx.compose.runtime.Immutable

@Immutable
enum class BudgetState {
  // Only local (not uploaded/synced)
  Local,

  // Unavailable locally, available to download
  Remote,

  // Downloaded & synced
  Synced,

  // Downloaded but broken group id (reset sync state)
  Detached,

  // user shouldn't have access to this file
  Broken,

  // user is offline so can't determine the status
  Unknown,
}
