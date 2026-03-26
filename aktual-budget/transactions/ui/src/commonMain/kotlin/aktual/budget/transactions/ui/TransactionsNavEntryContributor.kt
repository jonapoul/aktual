package aktual.budget.transactions.ui

import aktual.app.nav.NavEntryContributor
import aktual.app.nav.NavScope
import aktual.app.nav.TransactionsNavRoute
import aktual.app.nav.debugPop
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(NavScope::class)
class TransactionsNavEntryContributor : NavEntryContributor {
  override fun contribute(scope: EntryProviderScope<NavKey>, stack: SnapshotStateList<NavKey>) {
    scope.entry<TransactionsNavRoute> { route ->
      TransactionsScreen(TransactionsNavigatorImpl(stack), route.budgetId, route.token)
    }
  }
}

private class TransactionsNavigatorImpl(private val stack: SnapshotStateList<NavKey>) :
  TransactionsNavigator {
  override fun back() = stack.debugPop()
}
