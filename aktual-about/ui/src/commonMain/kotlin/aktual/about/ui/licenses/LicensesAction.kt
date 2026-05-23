package aktual.about.ui.licenses

import androidx.compose.runtime.Immutable

@Immutable internal sealed interface LicensesAction

internal data object NavBack : LicensesAction

internal data object Reload : LicensesAction

internal data object ToggleSearchBar : LicensesAction

@JvmInline internal value class EditSearchText(val text: String) : LicensesAction

@JvmInline internal value class LaunchUrl(val url: String) : LicensesAction

@Immutable
internal fun interface LicensesActionHandler {
  operator fun invoke(action: LicensesAction)
}
