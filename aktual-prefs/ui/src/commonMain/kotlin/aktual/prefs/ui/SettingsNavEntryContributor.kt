package aktual.prefs.ui

import aktual.core.nav.BackNavigator
import aktual.core.nav.CurrencySettingsNavRoute
import aktual.core.nav.CurrencySettingsNavigator
import aktual.core.nav.CustomThemeSettingsNavRoute
import aktual.core.nav.CustomThemesNavigator
import aktual.core.nav.FormatSettingsNavRoute
import aktual.core.nav.FormatSettingsNavigator
import aktual.core.nav.InspectThemeNavRoute
import aktual.core.nav.InspectThemeNavigator
import aktual.core.nav.NavEntryContributor
import aktual.core.nav.NavStack
import aktual.core.nav.ScheduleSettingsNavRoute
import aktual.core.nav.ScheduleSettingsNavigator
import aktual.core.nav.SearchThemeNavRoute
import aktual.core.nav.SearchThemeNavigator
import aktual.core.nav.SettingsNavRoute
import aktual.core.nav.SystemUiSettingsNavRoute
import aktual.core.nav.SystemUiSettingsNavigator
import aktual.core.nav.ThemeSettingsNavRoute
import aktual.core.nav.ThemeSettingsNavigator
import aktual.core.nav.TransactionSettingsNavRoute
import aktual.core.nav.TransactionSettingsNavigator
import aktual.di.AppScope
import aktual.prefs.ui.currency.CurrencySettingsScreen
import aktual.prefs.ui.format.FormatSettingsScreen
import aktual.prefs.ui.inspect.InspectThemeScreen
import aktual.prefs.ui.inspect.search.SearchThemeScreen
import aktual.prefs.ui.root.SettingsScreen
import aktual.prefs.ui.schedules.ScheduleSettingsScreen
import aktual.prefs.ui.systemui.SystemUiSettingsScreen
import aktual.prefs.ui.theme.ThemeSettingsScreen
import aktual.prefs.ui.theme.custom.CustomThemeSettingsScreen
import aktual.prefs.ui.transactions.TransactionSettingsScreen
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(AppScope::class)
class SettingsNavEntryContributor : NavEntryContributor {
  override fun EntryProviderScope<NavKey>.contribute(stack: NavStack<NavKey>) {
    entry<SettingsNavRoute> {
      SettingsScreen(
        back = BackNavigator(stack),
        toThemeSettings = ThemeSettingsNavigator(stack),
        toScheduleSettings = ScheduleSettingsNavigator(stack),
        toTransactionSettings = TransactionSettingsNavigator(stack),
        toSystemUiSettings = SystemUiSettingsNavigator(stack),
        toFormatSettings = FormatSettingsNavigator(stack),
        toCurrencySettings = CurrencySettingsNavigator(stack),
      )
    }

    entry<ScheduleSettingsNavRoute> { ScheduleSettingsScreen(BackNavigator(stack)) }

    entry<TransactionSettingsNavRoute> { TransactionSettingsScreen(BackNavigator(stack)) }

    entry<SystemUiSettingsNavRoute> { SystemUiSettingsScreen(BackNavigator(stack)) }

    entry<FormatSettingsNavRoute> { FormatSettingsScreen(BackNavigator(stack)) }

    entry<CurrencySettingsNavRoute> { CurrencySettingsScreen(BackNavigator(stack)) }

    entry<ThemeSettingsNavRoute> {
      ThemeSettingsScreen(
        back = BackNavigator(stack),
        toCustomThemes = CustomThemesNavigator(stack),
        toInspectTheme = InspectThemeNavigator(stack),
      )
    }

    entry<CustomThemeSettingsNavRoute> {
      CustomThemeSettingsScreen(BackNavigator(stack), InspectThemeNavigator(stack))
    }

    entry<InspectThemeNavRoute> { route ->
      InspectThemeScreen(
        back = BackNavigator(stack),
        toSearch = SearchThemeNavigator(stack),
        themeId = route.id,
      )
    }

    entry<SearchThemeNavRoute> { route -> SearchThemeScreen(BackNavigator(stack), route.id) }
  }
}
