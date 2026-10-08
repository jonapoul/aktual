package aktual.prefs.vm.inspect

import aktual.core.UrlOpener
import aktual.core.model.ThemeId
import aktual.core.theme.CustomColors
import aktual.core.theme.ThemeResolver
import aktual.di.AppScope
import aktual.prefs.vm.theme.properties
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

@Stable
@AssistedInject
class InspectThemeViewModel(
  @Assisted private val themeId: ThemeId,
  private val themeResolver: ThemeResolver,
  private val urlOpener: UrlOpener,
) : ViewModel() {
  private val mutableState = MutableStateFlow<InspectThemeState>(Loading)
  private val mutableSorting = MutableStateFlow<PropertySorting>(Default)

  val sorting: StateFlow<PropertySorting> = mutableSorting.asStateFlow()

  val state: StateFlow<InspectThemeState> =
    combine(mutableState, mutableSorting) { state, sorting ->
        if (state is Loaded) {
          state.copy(properties = state.properties.sorted(sorting))
        } else {
          state
        }
      }
      .stateIn(viewModelScope, Eagerly, initialValue = Loading)

  init {
    retry()
  }

  fun openRepo() {
    urlOpener("https://github.com/${themeId.value}")
  }

  fun setSorting(sorting: PropertySorting) {
    mutableSorting.update { sorting }
  }

  fun retry() {
    mutableState.update { Loading }
    viewModelScope.launch { loadTheme() }
  }

  private suspend fun loadTheme() {
    val colors = themeResolver.resolve(themeId)
    if (colors == null) {
      logcat.w { "Theme not found for id=$themeId" }
      mutableState.update { InspectThemeState.NotFound(themeId) }
      return
    }

    val isCustom = colors is CustomColors
    mutableState.update { InspectThemeState.Loaded(themeId, isCustom, colors.properties()) }
  }

  private fun ImmutableList<ThemeProperty>.sorted(
    sorting: PropertySorting,
  ): ImmutableList<ThemeProperty> =
    when (sorting) {
      Default -> this
      ByName -> sortedBy { it.name.lowercase() }.toImmutableList()
      ByColor ->
        sortedWith(compareBy({ it.color.hue() }, { it.color.luminance() })).toImmutableList()
    }

  // Hue in degrees from 0 to 360, with greys given -1 so they group together at the start
  @Suppress("MagicNumber")
  private fun Color.hue(): Float {
    val max = maxOf(red, green, blue)
    val delta = max - minOf(red, green, blue)
    if (delta == 0f) {
      return -1f
    }
    val hue =
      when (max) {
        red -> (green - blue) / delta % 6f
        green -> (blue - red) / delta + 2f
        else -> (red - green) / delta + 4f
      }
    return (hue * 60f + 360f) % 360f
  }

  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(AppScope::class)
  fun interface Factory : ManualViewModelAssistedFactory {
    fun create(@Assisted themeId: ThemeId): InspectThemeViewModel
  }
}
