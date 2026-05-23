package aktual.prefs.ui.theme.custom

import aktual.core.theme.CustomThemeSummary
import aktual.prefs.vm.theme.custom.ThemeFilter
import aktual.prefs.vm.theme.custom.ThemeSorting
import androidx.compose.runtime.Immutable

@Immutable internal sealed interface CustomThemeSettingsAction

internal data object NavBack : CustomThemeSettingsAction

internal data object RetryFetchCatalog : CustomThemeSettingsAction

internal data object DismissBottomSheet : CustomThemeSettingsAction

@JvmInline
internal value class InspectTheme(val summary: CustomThemeSummary) : CustomThemeSettingsAction

@JvmInline
internal value class SelectTheme(val summary: CustomThemeSummary) : CustomThemeSettingsAction

internal data object ShowFilterSheet : CustomThemeSettingsAction

internal data object ShowSortSheet : CustomThemeSettingsAction

@JvmInline internal value class SetModeFilter(val mode: ThemeFilter) : CustomThemeSettingsAction

@JvmInline internal value class SetSorting(val sorting: ThemeSorting) : CustomThemeSettingsAction

@Immutable
internal fun interface CustomThemeSettingsActionHandler {
  operator fun invoke(action: CustomThemeSettingsAction)
}
