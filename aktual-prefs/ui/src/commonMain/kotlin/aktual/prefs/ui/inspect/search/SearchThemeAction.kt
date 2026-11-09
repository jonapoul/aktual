package aktual.prefs.ui.inspect.search

internal sealed interface SearchThemeAction

internal data object NavBack : SearchThemeAction

@JvmInline internal value class SetQuery(val query: String) : SearchThemeAction
