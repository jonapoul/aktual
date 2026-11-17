package aktual.prefs.vm.search

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface SearchSettingsState {
  data object NoQuery : SearchSettingsState

  data object NoResults : SearchSettingsState

  data class Results(val query: String, val settings: ImmutableList<SettingsSearchResult>) :
    SearchSettingsState
}

enum class SettingsPage {
  Theme,
  SystemUi,
  Bars,
  Dialogs,
  Security,
  Format,
  Amounts,
  Dates,
  Transactions,
  LauncherIcon,
}

// Pages holding the open budget's synced settings rather than this device's
val BudgetSettingsPages: Set<SettingsPage> = setOf(Amounts, Dates)

enum class SettingsSearchKind {
  Page,
  Group,
  Preference,
}

// path holds the titles of the page and group containing the setting, outermost first. budgetName
// is set for settings synced with the open budget
@Immutable
data class SettingsSearchResult(
  val title: String,
  val kind: SettingsSearchKind,
  val page: SettingsPage,
  val path: ImmutableList<String>,
  val budgetName: String? = null,
) {
  val key: String
    get() = (path + title).joinToString(separator = "/")
}
