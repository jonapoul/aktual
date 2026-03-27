package aktual.prefs.ui

import aktual.app.nav.BackNavigator
import aktual.app.nav.InspectThemeNavRoute
import aktual.app.nav.InspectThemeNavigator
import aktual.app.nav.NavEntryContributor
import aktual.app.nav.NavScope
import aktual.app.nav.SettingsNavRoute
import aktual.app.nav.ThemeSettingsNavRoute
import aktual.app.nav.ThemeSettingsNavigator
import aktual.prefs.ui.inspect.InspectThemeScreen
import aktual.prefs.ui.root.SettingsScreen
import aktual.prefs.ui.theme.ThemeSettingsScreen
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(NavScope::class)
class SettingsNavEntryContributor : NavEntryContributor {
  override fun contribute(scope: EntryProviderScope<NavKey>, stack: SnapshotStateList<NavKey>) {
    scope.entry<SettingsNavRoute> {
      SettingsScreen(BackNavigator(stack), ThemeSettingsNavigator(stack))
    }

    scope.entry<ThemeSettingsNavRoute> {
      ThemeSettingsScreen(BackNavigator(stack), InspectThemeNavigator(stack))
    }

    scope.entry<InspectThemeNavRoute> { route ->
      InspectThemeScreen(BackNavigator(stack), route.id)
    }
  }
}
