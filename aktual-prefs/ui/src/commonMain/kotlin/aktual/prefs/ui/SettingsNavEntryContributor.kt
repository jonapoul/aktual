package aktual.prefs.ui

import aktual.app.nav.InspectThemeNavRoute
import aktual.app.nav.NavEntryContributor
import aktual.app.nav.NavScope
import aktual.app.nav.SettingsNavRoute
import aktual.app.nav.ThemeSettingsNavRoute
import aktual.app.nav.debugPop
import aktual.app.nav.debugPush
import aktual.core.model.ThemeId
import aktual.prefs.ui.inspect.InspectThemeNavigator
import aktual.prefs.ui.inspect.InspectThemeScreen
import aktual.prefs.ui.root.SettingsNavigator
import aktual.prefs.ui.root.SettingsScreen
import aktual.prefs.ui.theme.ThemeSettingsNavigator
import aktual.prefs.ui.theme.ThemeSettingsScreen
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(NavScope::class)
class SettingsNavEntryContributor : NavEntryContributor {
  override fun contribute(scope: EntryProviderScope<NavKey>, stack: SnapshotStateList<NavKey>) {
    scope.entry<SettingsNavRoute> { SettingsScreen(SettingsNavigatorImpl(stack)) }
    scope.entry<ThemeSettingsNavRoute> { ThemeSettingsScreen(ThemeSettingsNavigatorImpl(stack)) }
    scope.entry<InspectThemeNavRoute> { route ->
      InspectThemeScreen(InspectThemeNavigatorImpl(stack), route.id)
    }
  }
}

private class SettingsNavigatorImpl(private val stack: SnapshotStateList<NavKey>) :
  SettingsNavigator {
  override fun back() = stack.debugPop()

  override fun toThemeSettings() = stack.debugPush(ThemeSettingsNavRoute)
}

private class InspectThemeNavigatorImpl(private val stack: SnapshotStateList<NavKey>) :
  InspectThemeNavigator {
  override fun navBack() = stack.debugPop()
}

private class ThemeSettingsNavigatorImpl(private val stack: SnapshotStateList<NavKey>) :
  ThemeSettingsNavigator {
  override fun back() = stack.debugPop()

  override fun inspectTheme(id: ThemeId) = stack.debugPush(InspectThemeNavRoute(id))
}
