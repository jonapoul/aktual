package aktual.prefs.ui.inspect.search

import androidx.compose.runtime.Immutable

internal sealed interface SearchThemeAction

internal data object NavBack : SearchThemeAction

@JvmInline internal value class SetQuery(val query: String) : SearchThemeAction

@Immutable
internal fun interface SearchThemeActionHandler {
  operator fun invoke(action: SearchThemeAction)
}
