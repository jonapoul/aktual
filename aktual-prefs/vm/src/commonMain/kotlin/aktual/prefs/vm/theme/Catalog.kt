package aktual.prefs.vm.theme

import aktual.core.model.ThemeId
import aktual.theme.Colors
import aktual.theme.CustomThemeSummary
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class CatalogState(val items: ImmutableList<CatalogItem>, val status: CatalogStatus) {
  companion object {
    val Initial = CatalogState(persistentListOf(), Loading)
  }
}

enum class CatalogStatus {
  Idle,
  Loading,
  Failed,
}

@Immutable
data class CatalogItem(val summary: CustomThemeSummary, val download: DownloadState) {
  val id: ThemeId
    get() = ThemeId(summary.repo.id)
}

@Immutable
sealed interface DownloadState {
  data object Remote : DownloadState

  data object Downloading : DownloadState

  data object Downloaded : DownloadState

  @JvmInline value class Failed(val reason: String) : DownloadState
}

@Immutable
data class CatalogOptions(
  val mode: ThemeFilter = All,
  val download: DownloadFilter = AllThemes,
  val sorting: ThemeSorting = ByName,
)

enum class ThemeFilter {
  All,
  Light,
  Dark,
}

enum class DownloadFilter {
  AllThemes,
  DownloadedOnly,
  NotDownloaded,
}

enum class ThemeSorting {
  ByName,
  ByRepo,
  DownloadedFirst,
}

// A theme offered by the light or dark theme picker
@Immutable
data class ThemeOption(
  val id: ThemeId,
  // Null for the built-in themes, whose names come from the UI's strings
  val name: String?,
  val palette: ImmutableList<Color>,
  val isLocal: Boolean,
  // Null for themes that are already on the device
  val download: DownloadState?,
)

@Immutable
data class SystemThemePicker(
  val value: ThemeId,
  val options: ImmutableList<ThemeOption>,
  val enabled: Boolean,
)

@Immutable
sealed interface ThemeSettingsEvent {
  data object CatalogRefreshed : ThemeSettingsEvent

  @JvmInline value class ThemeDownloaded(val name: String) : ThemeSettingsEvent

  data class DownloadFailed(val name: String, val reason: String) : ThemeSettingsEvent

  @JvmInline value class InspectTheme(val id: ThemeId) : ThemeSettingsEvent

  // The light or dark theme picker can close
  data object ThemePicked : ThemeSettingsEvent
}

// The same six colours the catalog gives each theme
fun Colors.palette(): ImmutableList<Color> =
  persistentListOf(
    pageBackground,
    mobileHeaderBackground,
    buttonPrimaryBackground,
    tableBackground,
    pageText,
    settingsCardBackground,
  )
