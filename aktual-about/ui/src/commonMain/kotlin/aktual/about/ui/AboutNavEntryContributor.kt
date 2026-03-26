package aktual.about.ui

import aktual.about.ui.info.InfoNavigator
import aktual.about.ui.info.InfoScreen
import aktual.about.ui.licenses.LicensesNavigator
import aktual.about.ui.licenses.LicensesScreen
import aktual.app.nav.InfoNavRoute
import aktual.app.nav.LicensesNavRoute
import aktual.app.nav.NavEntryContributor
import aktual.app.nav.NavScope
import aktual.app.nav.debugPop
import aktual.app.nav.debugPush
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(NavScope::class)
class AboutNavEntryContributor : NavEntryContributor {
  override fun contribute(scope: EntryProviderScope<NavKey>, stack: SnapshotStateList<NavKey>) {
    scope.entry<InfoNavRoute> { InfoScreen(InfoNavigatorImpl(stack)) }
    scope.entry<LicensesNavRoute> { LicensesScreen(LicensesNavigatorImpl(stack)) }
  }
}

private class InfoNavigatorImpl(private val stack: SnapshotStateList<NavKey>) : InfoNavigator {
  override fun back() = stack.debugPop()

  override fun toLicenses() = stack.debugPush(LicensesNavRoute)
}

private class LicensesNavigatorImpl(private val stack: SnapshotStateList<NavKey>) :
  LicensesNavigator {
  override fun back() = stack.debugPop()
}
