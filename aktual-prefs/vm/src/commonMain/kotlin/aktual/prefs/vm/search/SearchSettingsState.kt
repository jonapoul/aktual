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
  Security,
  Format,
}

enum class SettingsSearchKind {
  Page,
  Group,
  Preference,
}

// path holds the titles of the page and group containing the setting, outermost first
@Immutable
data class SettingsSearchResult(
  val title: String,
  val kind: SettingsSearchKind,
  val page: SettingsPage,
  val path: ImmutableList<String>,
) {
  val key: String
    get() = (path + title).joinToString(separator = "/")
}
