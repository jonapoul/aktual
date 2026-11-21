package aktual.prefs.ui.theme

import aktual.core.model.ThemeId
import aktual.prefs.vm.theme.CatalogOptions
import aktual.theme.CustomThemeSummary

internal sealed interface ThemeSettingsAction

internal sealed interface PickerAction : ThemeSettingsAction

internal sealed interface CatalogAction : ThemeSettingsAction

internal data object NavBack : ThemeSettingsAction

@JvmInline internal value class InspectTheme(val id: ThemeId) : ThemeSettingsAction

@JvmInline internal value class SelectTheme(val id: ThemeId) : ThemeSettingsAction

@JvmInline internal value class SetUseSystemDefault(val value: Boolean) : ThemeSettingsAction

@JvmInline internal value class ShowPicker(val picker: PickerType) : PickerAction

internal data object DismissPicker : PickerAction

@JvmInline internal value class PickTheme(val id: ThemeId) : PickerAction

internal data object RefreshCatalog : CatalogAction

@JvmInline internal value class SelectCatalogTheme(val summary: CustomThemeSummary) : CatalogAction

@JvmInline internal value class InspectCatalogTheme(val summary: CustomThemeSummary) : CatalogAction

internal data object ShowCatalogOptions : CatalogAction

internal data object DismissCatalogOptions : CatalogAction

@JvmInline internal value class SetCatalogOptions(val options: CatalogOptions) : CatalogAction

@JvmInline internal value class ShowDownloadFailure(val failure: DownloadFailure) : CatalogAction

internal data object DismissDownloadFailure : CatalogAction

internal enum class PickerType {
  LightTheme,
  DarkTheme,
}
