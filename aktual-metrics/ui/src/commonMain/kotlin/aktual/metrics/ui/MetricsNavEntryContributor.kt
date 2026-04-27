package aktual.metrics.ui

import aktual.app.nav.AktualNavStack
import aktual.app.nav.BackNavigator
import aktual.app.nav.MetricsNavRoute
import aktual.app.nav.NavEntryContributor
import aktual.app.nav.NavScope
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(NavScope::class)
class MetricsNavEntryContributor : NavEntryContributor {
  override fun contribute(scope: EntryProviderScope<NavKey>, stack: AktualNavStack<NavKey>) {
    scope.entry<MetricsNavRoute> { MetricsScreen(BackNavigator(stack)) }
  }
}
