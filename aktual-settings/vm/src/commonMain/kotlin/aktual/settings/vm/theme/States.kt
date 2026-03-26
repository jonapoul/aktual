package aktual.settings.vm.theme

import aktual.core.model.ThemeId
import aktual.core.theme.CustomThemeSummary
import aktual.settings.vm.BooleanPreference
import aktual.settings.vm.ListPreference
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class ThemeSettingsState(
  val useSystemDefault: BooleanPreference,
  val darkTheme: ListPreference<ThemeId>,
  val constantTheme: ThemeId?,
)

enum class ThemeModeFilter {
  All,
  Light,
  Dark,
}

@Immutable
sealed interface CatalogState {
  data object Loading : CatalogState

  @JvmInline value class Failed(val reason: String) : CatalogState

  data class Success(val themes: ImmutableList<CatalogItem>, val modeFilter: ThemeModeFilter) :
    CatalogState
}

@Immutable
data class CatalogItem(
  val id: ThemeId,
  val summary: CustomThemeSummary,
  val isSelected: Boolean,
  val state: CustomThemeState,
)

sealed interface ThemeSettingsEvent {
  data object CacheRefreshed : ThemeSettingsEvent
}

@Immutable
sealed interface CustomThemeState {
  data object Fetching : CustomThemeState

  data object Cached : CustomThemeState

  @JvmInline value class Failed(val reason: String) : CustomThemeState
}
