package aktual.prefs.vm.inspect.search

import aktual.core.model.ThemeId
import aktual.core.theme.ThemeResolver
import aktual.di.AppScope
import aktual.prefs.vm.inspect.ThemeProperty
import aktual.prefs.vm.inspect.search.SearchThemeState.Results
import aktual.prefs.vm.inspect.toHexString
import aktual.prefs.vm.theme.properties
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Stable
@AssistedInject
class SearchThemeViewModel(
  @Assisted private val themeId: ThemeId,
  private val themeResolver: ThemeResolver,
) : ViewModel() {
  private val mutableQuery = MutableStateFlow("")
  private val properties = MutableStateFlow<List<ThemeProperty>>(emptyList())

  val query: StateFlow<String> = mutableQuery.asStateFlow()

  val state: StateFlow<SearchThemeState> =
    combine(mutableQuery, properties, ::search)
      .stateIn(viewModelScope, Eagerly, initialValue = NoQuery)

  init {
    viewModelScope.launch {
      val resolved = themeResolver.resolve(themeId)?.properties().orEmpty()
      properties.update { resolved }
    }
  }

  fun setQuery(query: String) {
    mutableQuery.update { query }
  }

  private fun search(query: String, properties: List<ThemeProperty>): SearchThemeState {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return NoQuery

    // Properties matching by name come before those only matching by colour
    val matching =
      properties
        .filter { property ->
          property.name.contains(trimmed, ignoreCase = true) ||
            property.color.toHexString().contains(trimmed, ignoreCase = true)
        }
        .sortedBy { !it.name.contains(trimmed, ignoreCase = true) }

    return if (matching.isEmpty()) {
      NoResults
    } else {
      Results(trimmed, matching.toImmutableList())
    }
  }

  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(AppScope::class)
  fun interface Factory : ManualViewModelAssistedFactory {
    fun create(@Assisted themeId: ThemeId): SearchThemeViewModel
  }
}
