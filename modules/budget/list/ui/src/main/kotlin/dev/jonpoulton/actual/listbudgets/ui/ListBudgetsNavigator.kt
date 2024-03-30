package dev.jonpoulton.actual.listbudgets.ui

import androidx.compose.runtime.Immutable

@Immutable
interface ListBudgetsNavigator {
  fun changeServer()
  fun logOut()
  fun openBudget()
}
