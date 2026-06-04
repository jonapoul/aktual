package aktual.prefs.ui.theme

import aktual.core.model.ThemeId
import androidx.compose.runtime.Immutable

@Immutable internal sealed interface ThemeSettingsAction

internal data object NavBack : ThemeSettingsAction

internal data object NavCustomThemes : ThemeSettingsAction

@JvmInline internal value class InspectTheme(val id: ThemeId) : ThemeSettingsAction

@JvmInline internal value class SelectTheme(val id: ThemeId) : ThemeSettingsAction

@JvmInline internal value class SetDarkTheme(val value: ThemeId) : ThemeSettingsAction

@JvmInline internal value class SetUseSystemDefault(val value: Boolean) : ThemeSettingsAction

@Immutable
internal fun interface ThemeSettingsActionHandler {
  operator fun invoke(action: ThemeSettingsAction)
}
