package aktual.prefs.ui.search

import aktual.core.nav.AmountSettingsNavRoute
import aktual.core.nav.AppIconSettingsNavRoute
import aktual.core.nav.BarSettingsNavRoute
import aktual.core.nav.DateSettingsNavRoute
import aktual.core.nav.DialogSettingsNavRoute
import aktual.core.nav.FormatSettingsNavRoute
import aktual.core.nav.NavStack
import aktual.core.nav.SecuritySettingsNavRoute
import aktual.core.nav.SettingsHighlight
import aktual.core.nav.SystemUiSettingsNavRoute
import aktual.core.nav.ThemeSettingsNavRoute
import aktual.core.nav.TransactionSettingsNavRoute
import aktual.core.nav.UpdateSettingsNavRoute
import aktual.prefs.vm.search.SettingsPage
import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey

// Opens a settings page from a search result
@Immutable
internal class SettingsPageNavigator(private val stack: NavStack<NavKey>) {
  operator fun invoke(page: SettingsPage, highlight: SettingsHighlight?) =
    stack.push(
      when (page) {
        Theme -> ThemeSettingsNavRoute(highlight)
        SystemUi -> SystemUiSettingsNavRoute(highlight)
        Bars -> BarSettingsNavRoute(highlight)
        Dialogs -> DialogSettingsNavRoute(highlight)
        Security -> SecuritySettingsNavRoute(highlight)
        Format -> FormatSettingsNavRoute(highlight)
        Amounts -> AmountSettingsNavRoute(highlight)
        Dates -> DateSettingsNavRoute(highlight)
        Transactions -> TransactionSettingsNavRoute(highlight)
        LauncherIcon -> AppIconSettingsNavRoute(highlight)
        Updates -> UpdateSettingsNavRoute(highlight)
      },
    )
}
