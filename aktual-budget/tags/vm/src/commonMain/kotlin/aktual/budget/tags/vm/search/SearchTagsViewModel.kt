package aktual.budget.tags.vm.search

import aktual.budget.db.dao.TagsDao
import aktual.budget.tags.vm.list.TagItem
import aktual.budget.tags.vm.list.toTagItem
import aktual.budget.tags.vm.search.SearchTagsState.Results
import aktual.core.snippet
import aktual.di.BudgetScope
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactoryKey
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Stable
@AssistedInject
class SearchTagsViewModel
internal constructor(
  @Assisted private val savedState: SavedStateHandle,
  tagsDao: TagsDao,
) : ViewModel() {
  @AssistedFactory
  @ViewModelAssistedFactoryKey(SearchTagsViewModel::class)
  @ContributesIntoMap(BudgetScope::class)
  fun interface Factory : ViewModelAssistedFactory {
    override fun create(extras: CreationExtras): SearchTagsViewModel =
      create(extras.createSavedStateHandle())

    fun create(@Assisted savedState: SavedStateHandle): SearchTagsViewModel
  }

  val query: StateFlow<String> = savedState.getStateFlow(KEY_QUERY, initialValue = "")

  private val tags: Flow<List<TagItem>> =
    tagsDao.observeTags().map { rows -> rows.mapNotNull { it.toTagItem() } }

  val state: StateFlow<SearchTagsState> =
    combine(query, tags, ::search).stateIn(viewModelScope, Eagerly, initialValue = NoQuery)

  fun setQuery(query: String) {
    savedState[KEY_QUERY] = query
  }

  private fun search(query: String, tags: List<TagItem>): SearchTagsState {
    val trimmed = query.trim().removePrefix("#")
    if (trimmed.isEmpty()) return NoQuery

    // Tags matching by name come before those only matching by description
    val matching =
      tags
        .filter {
          it.tag.contains(trimmed, ignoreCase = true) ||
            it.description.contains(trimmed, ignoreCase = true)
        }
        .sortedWith(
          compareBy<TagItem> { !it.tag.contains(trimmed, ignoreCase = true) }
            .thenBy { it.tag.lowercase() }
        )
        .map { it.copy(description = snippet(it.description, trimmed, lead = DESCRIPTION_LEAD)) }

    return if (matching.isEmpty()) {
      NoResults
    } else {
      Results(trimmed, matching.toImmutableList())
    }
  }

  private companion object {
    const val KEY_QUERY = "query"

    // Descriptions share a single line with the tag pill, so keep less context before the match
    const val DESCRIPTION_LEAD = 8
  }
}
