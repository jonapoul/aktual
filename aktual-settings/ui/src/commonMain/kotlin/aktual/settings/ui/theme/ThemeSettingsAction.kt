package aktual.settings.ui.theme

import aktual.core.model.ThemeId
import aktual.settings.vm.theme.ThemeModeFilter
import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface ThemeSettingsAction {
  data object NavBack : ThemeSettingsAction

  data object RetryFetchCatalog : ThemeSettingsAction

  data object ClearCache : ThemeSettingsAction

  @JvmInline value class InspectTheme(val id: ThemeId) : ThemeSettingsAction

  @JvmInline value class SelectTheme(val id: ThemeId) : ThemeSettingsAction

  @JvmInline value class SetDarkTheme(val value: ThemeId) : ThemeSettingsAction

  @JvmInline value class SetUseSystemDefault(val value: Boolean) : ThemeSettingsAction

  @JvmInline value class SetModeFilter(val filter: ThemeModeFilter) : ThemeSettingsAction
}
