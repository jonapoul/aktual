package aktual.prefs.ui.theme.custom

import aktual.core.theme.CustomThemeSummary
import aktual.prefs.vm.theme.custom.ThemeFilter
import aktual.prefs.vm.theme.custom.ThemeSorting
import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface CustomThemeSettingsAction {
  data object NavBack : CustomThemeSettingsAction

  data object RetryFetchCatalog : CustomThemeSettingsAction

  data object DismissBottomSheet : CustomThemeSettingsAction

  @JvmInline value class InspectTheme(val summary: CustomThemeSummary) : CustomThemeSettingsAction

  @JvmInline value class SelectTheme(val summary: CustomThemeSummary) : CustomThemeSettingsAction

  data object ShowFilterSheet : CustomThemeSettingsAction

  data object ShowSortSheet : CustomThemeSettingsAction

  @JvmInline value class SetModeFilter(val mode: ThemeFilter) : CustomThemeSettingsAction

  @JvmInline value class SetSorting(val sorting: ThemeSorting) : CustomThemeSettingsAction
}
