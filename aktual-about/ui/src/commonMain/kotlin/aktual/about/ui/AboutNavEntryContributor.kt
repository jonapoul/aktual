package aktual.about.ui

import aktual.about.ui.info.InfoScreen
import aktual.about.ui.licenses.LicensesScreen
import aktual.app.nav.BackNavigator
import aktual.app.nav.InfoNavRoute
import aktual.app.nav.LicensesNavRoute
import aktual.app.nav.LicensesNavigator
import aktual.app.nav.NavEntryContributor
import aktual.app.nav.NavScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(NavScope::class)
class AboutNavEntryContributor : NavEntryContributor {
  override fun contribute(scope: EntryProviderScope<NavKey>, stack: SnapshotStateList<NavKey>) {
    scope.entry<InfoNavRoute> { InfoScreen(BackNavigator(stack), LicensesNavigator(stack)) }
    scope.entry<LicensesNavRoute> { LicensesScreen(BackNavigator(stack)) }
  }
}
