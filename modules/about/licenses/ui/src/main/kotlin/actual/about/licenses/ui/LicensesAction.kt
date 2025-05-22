package actual.about.licenses.ui

import androidx.compose.runtime.Immutable
import dev.drewhamilton.poko.Poko

@Immutable
internal sealed interface LicensesAction {
  data object NavBack : LicensesAction
  data object Reload : LicensesAction
  data object ToggleSearchBar : LicensesAction
  @Poko class EditSearchText(val text: String) : LicensesAction
  @Poko class LaunchUrl(val url: String) : LicensesAction
}
