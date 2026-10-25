package aktual.prefs.vm.inspect

import aktual.core.UuidGenerator
import aktual.core.model.ThemeId
import aktual.core.theme.Colors
import aktual.core.theme.CustomColors
import aktual.core.theme.CustomThemeCache
import aktual.core.theme.CustomThemeRepo
import aktual.core.theme.ThemeResolver
import aktual.core.theme.toCustomColors
import aktual.di.AppScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.prefs.ThemePreferences
import aktual.prefs.vm.inspect.InspectThemeEvent.Deleted
import aktual.prefs.vm.inspect.InspectThemeEvent.Duplicated
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import logcat.logcat

@Stable
@AssistedInject
class InspectThemeViewModel(
  @Assisted private val themeId: ThemeId,
  private val themeResolver: ThemeResolver,
  private val cache: CustomThemeCache,
  private val preferences: ThemePreferences,
  private val uuidGenerator: UuidGenerator,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  private val mutableState = MutableStateFlow<InspectThemeState>(Loading)
  private val mutableSorting = MutableStateFlow<PropertySorting>(Default)
  private val saveMutex = Mutex()
  private var colors: Colors? = null

  private val mutableEvents =
    MutableSharedFlow<InspectThemeEvent>(
      replay = 0,
      extraBufferCapacity = 1,
      onBufferOverflow = DROP_OLDEST,
    )

  val events: SharedFlow<InspectThemeEvent> = mutableEvents

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

  val repoUrl: String = "https://github.com/${themeId.value}"

  fun setSorting(sorting: PropertySorting) {
    mutableSorting.update { sorting }
  }

  fun retry() {
    mutableState.update { Loading }
    viewModelScope.launch { loadTheme() }
  }

  // Copies the theme into a new local one, leaving the original untouched
  fun duplicate(name: String) {
    val source = colors ?: return
    viewModelScope.launch {
      val copy = source.toCustomColors(name, CustomThemeRepo.local(uuidGenerator()))
      cache.save(copy)
      mutableEvents.tryEmit(Duplicated(copy.id))
    }
  }

  fun setColor(attribute: String, color: Color) {
    val local = localColors() ?: return
    val edited = local.toCustomColors(local.name, local.repo, overrides = mapOf(attribute to color))
    setLoaded(edited)
    viewModelScope.launch { saveMutex.withLock { cache.save(edited) } }
  }

  fun delete() {
    val local = localColors() ?: return
    viewModelScope.launch {
      if (preferences.constantTheme.get() == themeId) {
        preferences.constantTheme.set(null)
      }
      saveMutex.withLock { cache.delete(local.repo) }
      mutableEvents.tryEmit(Deleted)
    }
  }

  private fun localColors(): CustomColors? {
    val custom = colors as? CustomColors
    return custom?.takeIf { it.repo.isLocal }
  }

  private suspend fun loadTheme() {
    val colors = themeResolver.resolve(themeId)
    if (colors == null) {
      logcat.w { "Theme not found for id=$themeId" }
      mutableState.update { InspectThemeState.NotFound(themeId) }
      return
    }
    setLoaded(colors)
  }

  private fun setLoaded(colors: Colors) {
    this.colors = colors
    val source: ThemeSource =
      when {
        colors !is CustomColors -> BuiltIn
        colors.repo.isLocal -> Local
        else -> Downloaded
      }
    val name = (colors as? CustomColors)?.name
    mutableState.update { InspectThemeState.Loaded(themeId, name, source, colors.properties()) }
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
