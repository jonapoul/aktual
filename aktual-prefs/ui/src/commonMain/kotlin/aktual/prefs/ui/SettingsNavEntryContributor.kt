package aktual.prefs.ui

import aktual.core.nav.BackNavigator
import aktual.core.nav.CustomThemeSettingsNavRoute
import aktual.core.nav.CustomThemesNavigator
import aktual.core.nav.InspectThemeNavRoute
import aktual.core.nav.InspectThemeNavigator
import aktual.core.nav.NavEntryContributor
import aktual.core.nav.NavStack
import aktual.core.nav.SettingsNavRoute
import aktual.core.nav.ThemeSettingsNavRoute
import aktual.core.nav.ThemeSettingsNavigator
import aktual.di.AppScope
import aktual.prefs.ui.inspect.InspectThemeScreen
import aktual.prefs.ui.root.SettingsScreen
import aktual.prefs.ui.theme.ThemeSettingsScreen
import aktual.prefs.ui.theme.custom.CustomThemeSettingsScreen
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(AppScope::class)
class SettingsNavEntryContributor : NavEntryContributor {
  override fun contribute(scope: EntryProviderScope<NavKey>, stack: NavStack<NavKey>) {
    scope.entry<SettingsNavRoute> {
      SettingsScreen(BackNavigator(stack), ThemeSettingsNavigator(stack))
    }

    scope.entry<ThemeSettingsNavRoute> {
      ThemeSettingsScreen(
        back = BackNavigator(stack),
        toCustomThemes = CustomThemesNavigator(stack),
        toInspectTheme = InspectThemeNavigator(stack),
      )
    }

    scope.entry<CustomThemeSettingsNavRoute> {
      CustomThemeSettingsScreen(BackNavigator(stack), InspectThemeNavigator(stack))
    }

    scope.entry<InspectThemeNavRoute> { route ->
      InspectThemeScreen(BackNavigator(stack), route.id)
    }
  }
}
