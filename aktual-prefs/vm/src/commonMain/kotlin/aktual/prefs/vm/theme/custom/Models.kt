package aktual.prefs.vm.theme.custom

import aktual.core.model.ThemeId
import aktual.core.theme.CustomThemeSummary
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface CatalogState {
  data object Loading : CatalogState

  data class Success(val items: ImmutableList<CatalogItem>) : CatalogState

  @Immutable
  sealed interface Failed : CatalogState {
    data object FetchingCatalog : Failed
  }
}

@Immutable
data class CatalogItem(
  val id: ThemeId,
  val summary: CustomThemeSummary,
  val isSelected: Boolean,
  val state: CacheState,
)

enum class ThemeFilter {
  All,
  Light,
  Dark,
}

@Immutable
sealed interface CacheState {
  data object Remote : CacheState

  data object Fetching : CacheState

  @JvmInline value class Failed(val reason: String) : CacheState

  @JvmInline value class Cached(val summary: CustomThemeSummary) : CacheState
}

@Immutable
sealed interface CustomThemeEvent {
  data object CacheRefreshed : CustomThemeEvent

  @JvmInline value class InspectTheme(val id: ThemeId) : CustomThemeEvent

  data class FailedFetching(val reason: String, val name: String) : CustomThemeEvent
}
