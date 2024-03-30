package dev.jonpoulton.actual.login.ui

import androidx.compose.runtime.Immutable

@Immutable
interface LoginNavigator {
  fun changeServer()
  fun listBudgets()
}
