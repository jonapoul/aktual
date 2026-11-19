package aktual.prefs.ui

import aktual.core.nav.AdvancedSettingsNavRoute
import aktual.core.nav.AdvancedSettingsNavigator
import aktual.core.nav.AmountSettingsNavRoute
import aktual.core.nav.AmountSettingsNavigator
import aktual.core.nav.AppIconSettingsNavRoute
import aktual.core.nav.AppIconSettingsNavigator
import aktual.core.nav.BackNavigator
import aktual.core.nav.BarSettingsNavRoute
import aktual.core.nav.BarSettingsNavigator
import aktual.core.nav.CustomThemeSettingsNavRoute
import aktual.core.nav.CustomThemesNavigator
import aktual.core.nav.DateSettingsNavRoute
import aktual.core.nav.DateSettingsNavigator
import aktual.core.nav.DialogSettingsNavRoute
import aktual.core.nav.DialogSettingsNavigator
import aktual.core.nav.ExperimentalSettingsNavRoute
import aktual.core.nav.ExperimentalSettingsNavigator
import aktual.core.nav.FormatSettingsNavRoute
import aktual.core.nav.FormatSettingsNavigator
import aktual.core.nav.InspectThemeNavRoute
import aktual.core.nav.InspectThemeNavigator
import aktual.core.nav.NavEntryContributor
import aktual.core.nav.NavStack
import aktual.core.nav.SearchSettingsNavRoute
import aktual.core.nav.SearchSettingsNavigator
import aktual.core.nav.SearchThemeNavRoute
import aktual.core.nav.SearchThemeNavigator
import aktual.core.nav.SecuritySettingsNavRoute
import aktual.core.nav.SecuritySettingsNavigator
import aktual.core.nav.SettingsNavRoute
import aktual.core.nav.SystemUiSettingsNavRoute
import aktual.core.nav.SystemUiSettingsNavigator
import aktual.core.nav.ThemeSettingsNavRoute
import aktual.core.nav.ThemeSettingsNavigator
import aktual.core.nav.TransactionSettingsNavRoute
import aktual.core.nav.TransactionSettingsNavigator
import aktual.core.nav.UpdateSettingsNavRoute
import aktual.core.nav.UpdateSettingsNavigator
import aktual.di.AppScope
import aktual.prefs.ui.advanced.AdvancedSettingsScreen
import aktual.prefs.ui.amounts.AmountSettingsScreen
import aktual.prefs.ui.appicon.AppIconSettingsScreen
import aktual.prefs.ui.bars.BarSettingsScreen
import aktual.prefs.ui.dates.DateSettingsScreen
import aktual.prefs.ui.dialogs.DialogSettingsScreen
import aktual.prefs.ui.experimental.ExperimentalSettingsScreen
import aktual.prefs.ui.format.FormatSettingsScreen
import aktual.prefs.ui.inspect.InspectThemeScreen
import aktual.prefs.ui.inspect.search.SearchThemeScreen
import aktual.prefs.ui.root.SettingsScreen
import aktual.prefs.ui.search.SearchSettingsScreen
import aktual.prefs.ui.search.SettingsPageNavigator
import aktual.prefs.ui.security.SecuritySettingsScreen
import aktual.prefs.ui.systemui.SystemUiSettingsScreen
import aktual.prefs.ui.theme.ThemeSettingsScreen
import aktual.prefs.ui.theme.custom.CustomThemeSettingsScreen
import aktual.prefs.ui.transactions.TransactionSettingsScreen
import aktual.prefs.ui.updates.UpdateSettingsScreen
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(AppScope::class)
class SettingsNavEntryContributor : NavEntryContributor {
  override fun EntryProviderScope<NavKey>.contribute(stack: NavStack<NavKey>) {
    entry<SettingsNavRoute> {
      SettingsScreen(
        back = BackNavigator(stack),
        toSearch = SearchSettingsNavigator(stack),
        toThemeSettings = ThemeSettingsNavigator(stack),
        toSystemUiSettings = SystemUiSettingsNavigator(stack),
        toSecuritySettings = SecuritySettingsNavigator(stack),
        toFormatSettings = FormatSettingsNavigator(stack),
        toTransactionSettings = TransactionSettingsNavigator(stack),
        toAppIconSettings = AppIconSettingsNavigator(stack),
        toAmountSettings = AmountSettingsNavigator(stack),
        toDateSettings = DateSettingsNavigator(stack),
        toUpdateSettings = UpdateSettingsNavigator(stack),
        toAdvancedSettings = AdvancedSettingsNavigator(stack),
        toExperimentalSettings = ExperimentalSettingsNavigator(stack),
      )
    }

    entry<SearchSettingsNavRoute> {
      SearchSettingsScreen(back = BackNavigator(stack), toPage = SettingsPageNavigator(stack))
    }

    entry<SystemUiSettingsNavRoute> { route ->
      SystemUiSettingsScreen(
        back = BackNavigator(stack),
        toBarSettings = BarSettingsNavigator(stack),
        toDialogSettings = DialogSettingsNavigator(stack),
        highlight = route.highlight,
      )
    }

    entry<BarSettingsNavRoute> { route ->
      BarSettingsScreen(BackNavigator(stack), route.highlight)
    }

    entry<DateSettingsNavRoute> { route ->
      DateSettingsScreen(BackNavigator(stack), route.highlight)
    }

    entry<TransactionSettingsNavRoute> { route ->
      TransactionSettingsScreen(BackNavigator(stack), route.highlight)
    }

    entry<DialogSettingsNavRoute> { route ->
      DialogSettingsScreen(BackNavigator(stack), route.highlight)
    }

    entry<SecuritySettingsNavRoute> { route ->
      SecuritySettingsScreen(BackNavigator(stack), route.highlight)
    }

    entry<FormatSettingsNavRoute> { route ->
      FormatSettingsScreen(BackNavigator(stack), route.highlight)
    }

    entry<AmountSettingsNavRoute> { route ->
      AmountSettingsScreen(BackNavigator(stack), route.highlight)
    }

    entry<UpdateSettingsNavRoute> { route ->
      UpdateSettingsScreen(BackNavigator(stack), route.highlight)
    }

    entry<AdvancedSettingsNavRoute> { AdvancedSettingsScreen(BackNavigator(stack)) }

    entry<ExperimentalSettingsNavRoute> { ExperimentalSettingsScreen(BackNavigator(stack)) }

    entry<AppIconSettingsNavRoute> { route ->
      AppIconSettingsScreen(BackNavigator(stack), route.highlight)
    }

    entry<ThemeSettingsNavRoute> { route ->
      ThemeSettingsScreen(
        back = BackNavigator(stack),
        toCustomThemes = CustomThemesNavigator(stack),
        toInspectTheme = InspectThemeNavigator(stack),
        highlight = route.highlight,
      )
    }

    entry<CustomThemeSettingsNavRoute> {
      CustomThemeSettingsScreen(BackNavigator(stack), InspectThemeNavigator(stack))
    }

    entry<InspectThemeNavRoute> { route ->
      InspectThemeScreen(
        back = BackNavigator(stack),
        toSearch = SearchThemeNavigator(stack),
        toInspectTheme = InspectThemeNavigator(stack),
        themeId = route.id,
      )
    }

    entry<SearchThemeNavRoute> { route -> SearchThemeScreen(BackNavigator(stack), route.id) }
  }
}
