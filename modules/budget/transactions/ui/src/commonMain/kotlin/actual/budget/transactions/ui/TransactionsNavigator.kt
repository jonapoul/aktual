package actual.budget.transactions.ui

import androidx.compose.runtime.Immutable

@Immutable
interface TransactionsNavigator {
  fun back(): Boolean
}
