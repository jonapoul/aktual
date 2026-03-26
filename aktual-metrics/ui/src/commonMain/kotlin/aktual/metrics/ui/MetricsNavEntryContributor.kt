package aktual.metrics.ui

import aktual.app.nav.MetricsNavRoute
import aktual.app.nav.NavEntryContributor
import aktual.app.nav.NavScope
import aktual.app.nav.debugPop
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(NavScope::class)
class MetricsNavEntryContributor : NavEntryContributor {
  override fun contribute(scope: EntryProviderScope<NavKey>, stack: SnapshotStateList<NavKey>) {
    scope.entry<MetricsNavRoute> { MetricsScreen(MetricsNavigatorImpl(stack)) }
  }
}

private class MetricsNavigatorImpl(private val stack: SnapshotStateList<NavKey>) :
  MetricsNavigator {
  override fun back() = stack.debugPop()
}
