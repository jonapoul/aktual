package aktual.prefs.vm.theme

import aktual.core.model.ThemeId
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.ListPreference
import androidx.compose.runtime.Immutable

@Immutable
data class ThemeSettingsState(
  val useSystemDefault: BooleanPreference,
  val darkTheme: ListPreference<ThemeId>,
  val constantTheme: ThemeId?,
)
