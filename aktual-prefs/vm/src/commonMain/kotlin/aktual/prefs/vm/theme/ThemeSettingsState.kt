package aktual.prefs.vm.theme

import aktual.core.model.ThemeId
import aktual.core.theme.Colors
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.ListPreference
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class ThemeSettingsState(
  val useSystemDefault: BooleanPreference,
  val darkTheme: ListPreference<ThemeId>,
  val constantTheme: ThemeId?,
  val localThemes: ImmutableList<LocalTheme>,
)

@Immutable data class LocalTheme(val id: ThemeId, val name: String, val swatch: ThemeSwatch)

// A few colours that tell themes apart at a glance
@Immutable data class ThemeSwatch(val background: Color, val surface: Color, val accent: Color)

fun Colors.swatch(): ThemeSwatch =
  ThemeSwatch(
    background = pageBackground,
    surface = settingsCardBackground,
    accent = buttonPrimaryBackground,
  )
