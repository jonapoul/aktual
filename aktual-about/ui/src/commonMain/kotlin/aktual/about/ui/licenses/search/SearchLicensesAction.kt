package aktual.about.ui.licenses.search

import androidx.compose.runtime.Immutable

internal sealed interface SearchLicensesAction

internal data object NavBack : SearchLicensesAction

@JvmInline internal value class SetQuery(val query: String) : SearchLicensesAction

@JvmInline internal value class LaunchUrl(val url: String) : SearchLicensesAction

@Immutable
internal fun interface SearchLicensesActionHandler {
  operator fun invoke(action: SearchLicensesAction)
}
