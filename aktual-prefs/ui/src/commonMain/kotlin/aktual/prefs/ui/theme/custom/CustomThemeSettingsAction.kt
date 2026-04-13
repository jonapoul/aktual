package aktual.prefs.ui.theme.custom

import aktual.core.theme.CustomThemeSummary
import aktual.prefs.vm.theme.custom.ThemeFilter
import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface CustomThemeSettingsAction {
  data object NavBack : CustomThemeSettingsAction

  data object RetryFetchCatalog : CustomThemeSettingsAction

  data object DismissBottomSheet : CustomThemeSettingsAction

  @JvmInline value class InspectTheme(val summary: CustomThemeSummary) : CustomThemeSettingsAction

  @JvmInline value class SelectTheme(val summary: CustomThemeSummary) : CustomThemeSettingsAction

  data object ShowFilterSheet : CustomThemeSettingsAction

  @JvmInline value class SetModeFilter(val mode: ThemeFilter) : CustomThemeSettingsAction
}
