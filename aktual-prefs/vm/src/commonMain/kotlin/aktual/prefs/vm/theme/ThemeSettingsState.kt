package aktual.prefs.vm.theme

import aktual.core.model.ThemeId
import aktual.core.theme.Colors
import aktual.prefs.vm.BooleanPreference
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap

@Immutable
data class ThemeSettingsState(
  val useSystemDefault: BooleanPreference,
  val lightTheme: SystemThemePicker,
  val darkTheme: SystemThemePicker,
  val constantTheme: ThemeId?,
  val localThemes: ImmutableList<LocalTheme>,
  // Filtered and sorted by catalogOptions
  val catalog: CatalogState,
  val catalogOptions: CatalogOptions,
  // One while a single theme is used, or the light then dark theme while matching the system
  val previews: ImmutableList<ThemePreview>,
  // Names of the local and catalog themes. Built-in names come from the UI's strings
  val customThemeNames: ImmutableMap<ThemeId, String>,
)

@Immutable
data class ThemePreview(val id: ThemeId, val mode: ThemePreviewMode, val swatch: ThemeSwatch)

enum class ThemePreviewMode {
  Always,
  Day,
  Night,
}

@Immutable data class LocalTheme(val id: ThemeId, val name: String, val swatch: ThemeSwatch)

// A few colours that tell themes apart at a glance
@Immutable
data class ThemeSwatch(
  val background: Color,
  val header: Color,
  val surface: Color,
  val accent: Color,
)

fun Colors.swatch(): ThemeSwatch =
  ThemeSwatch(
    background = pageBackground,
    header = mobileHeaderBackground,
    surface = settingsCardBackground,
    accent = buttonPrimaryBackground,
  )
