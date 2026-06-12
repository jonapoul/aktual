package aktual.metrics.ui

import aktual.core.nav.BackNavigator
import aktual.core.nav.MetricsNavRoute
import aktual.core.nav.NavEntryContributor
import aktual.core.nav.NavStack
import aktual.di.AppScope
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(AppScope::class)
class MetricsNavEntryContributor : NavEntryContributor {
  override fun EntryProviderScope<NavKey>.contribute(stack: NavStack<NavKey>) {
    entry<MetricsNavRoute> { MetricsScreen(BackNavigator(stack)) }
  }
}
