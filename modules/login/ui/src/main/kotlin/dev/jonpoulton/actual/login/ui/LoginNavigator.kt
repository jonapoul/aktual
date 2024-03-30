package dev.jonpoulton.actual.login.ui

import androidx.compose.runtime.Immutable

@Immutable
interface LoginNavigator {
  fun navBack()
  fun changeServer()
  fun listBudgets()
}
