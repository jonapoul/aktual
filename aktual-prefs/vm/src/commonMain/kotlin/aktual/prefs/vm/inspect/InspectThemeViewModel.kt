package aktual.prefs.vm.inspect

import aktual.core.model.ThemeId
import aktual.core.model.UrlOpener
import aktual.core.theme.CustomTheme
import aktual.core.theme.ThemeResolver
import aktual.prefs.vm.theme.properties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

@AssistedInject
class InspectThemeViewModel(
  @Assisted private val themeId: ThemeId,
  private val themeResolver: ThemeResolver,
  private val urlOpener: UrlOpener,
) : ViewModel() {
  private val mutableState = MutableStateFlow<InspectThemeState>(InspectThemeState.Loading)
  val state: StateFlow<InspectThemeState> = mutableState.asStateFlow()

  init {
    retry()
  }

  fun openRepo() {
    urlOpener("https://github.com/${themeId.value}")
  }

  fun retry() {
    mutableState.update { InspectThemeState.Loading }
    viewModelScope.launch { loadTheme() }
  }

  private suspend fun loadTheme() {
    val theme = themeResolver.resolve(themeId)
    if (theme == null) {
      logcat.w { "Theme not found for id=$themeId" }
      mutableState.update { InspectThemeState.NotFound(themeId) }
      return
    }

    val isCustom = theme is CustomTheme
    mutableState.update { InspectThemeState.Loaded(themeId, isCustom, theme.properties()) }
  }

  @AssistedFactory
  @ManualViewModelAssistedFactoryKey(Factory::class)
  @ContributesIntoMap(AppScope::class)
  fun interface Factory : ManualViewModelAssistedFactory {
    fun create(@Assisted themeId: ThemeId): InspectThemeViewModel
  }
}
