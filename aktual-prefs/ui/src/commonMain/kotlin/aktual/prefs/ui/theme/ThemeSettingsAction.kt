package aktual.prefs.ui.theme

import aktual.core.model.ThemeId
import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface ThemeSettingsAction {
  data object NavBack : ThemeSettingsAction

  data object NavCustomThemes : ThemeSettingsAction

  @JvmInline value class InspectTheme(val id: ThemeId) : ThemeSettingsAction

  @JvmInline value class SelectTheme(val id: ThemeId) : ThemeSettingsAction

  @JvmInline value class SetDarkTheme(val value: ThemeId) : ThemeSettingsAction

  @JvmInline value class SetUseSystemDefault(val value: Boolean) : ThemeSettingsAction
}
