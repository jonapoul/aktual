package aktual.prefs.ui.search

import aktual.prefs.vm.search.SettingsSearchResult
import androidx.compose.runtime.Immutable

internal sealed interface SearchSettingsAction

internal data object NavBack : SearchSettingsAction

@JvmInline internal value class SetQuery(val query: String) : SearchSettingsAction

@JvmInline
internal value class OpenSetting(val setting: SettingsSearchResult) : SearchSettingsAction

@Immutable
internal fun interface SearchSettingsActionHandler {
  operator fun invoke(action: SearchSettingsAction)
}
