package aktual.about.ui.licenses.search

internal sealed interface SearchLicensesAction

internal data object NavBack : SearchLicensesAction

@JvmInline internal value class SetQuery(val query: String) : SearchLicensesAction

@JvmInline internal value class LaunchUrl(val url: String) : SearchLicensesAction
