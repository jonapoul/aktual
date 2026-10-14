package aktual.prefs.vm.theme

import aktual.core.model.ThemeId
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.ListPreference
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class ThemeSettingsState(
  val useSystemDefault: BooleanPreference,
  val darkTheme: ListPreference<ThemeId>,
  val constantTheme: ThemeId?,
  val localThemes: ImmutableList<LocalTheme>,
)

@Immutable data class LocalTheme(val id: ThemeId, val name: String)
