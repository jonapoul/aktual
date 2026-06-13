package aktual.budget.tags.vm.list

import aktual.budget.db.dao.TagsDao
import aktual.di.BudgetScope
import alakazam.kotlin.requireMessage
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import app.cash.molecule.RecompositionMode.Immediate
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactoryKey
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

@Stable
@AssistedInject
class ListTagsViewModel(
  @Assisted private val savedState: SavedStateHandle,
  private val tagsDao: TagsDao,
) : ViewModel() {
  @AssistedFactory
  @ViewModelAssistedFactoryKey(ListTagsViewModel::class)
  @ContributesIntoMap(BudgetScope::class)
  fun interface Factory : ViewModelAssistedFactory {
    override fun create(extras: CreationExtras): ListTagsViewModel =
      create(extras.createSavedStateHandle())

    fun create(@Assisted savedState: SavedStateHandle): ListTagsViewModel
  }

  private val mutableTags = MutableStateFlow<ImmutableList<TagItem>>(persistentListOf())
  private val mutableIsLoading = MutableStateFlow(true)
  private val mutableFailure = MutableStateFlow<String?>(null)
  private var reloadJob: Job? = null

  private val filterText = savedState.getStateFlow(KEY_FILTER_TEXT, initialValue = "")
  private val isSearchActive = savedState.getStateFlow(KEY_IS_SEARCH_ACTIVE, initialValue = false)

  val state: StateFlow<ListTagsState> =
    viewModelScope.launchMolecule(Immediate) {
      val tags by mutableTags.collectAsState()
      val isLoading by mutableIsLoading.collectAsState()
      val failure by mutableFailure.collectAsState()
      val filterText by filterText.collectAsState()
      val isSearchActive by isSearchActive.collectAsState()
      @Suppress("BracesOnWhenStatements")
      when {
        isLoading -> Loading
        failure != null -> Failure(failure)
        tags.isEmpty() -> Empty
        else -> buildSuccessState(isSearchActive, tags, filterText)
      }
    }

  init {
    reload()
  }

  fun openSearch() {
    savedState[KEY_IS_SEARCH_ACTIVE] = true
  }

  fun setFilterText(text: String) {
    savedState[KEY_FILTER_TEXT] = text
  }

  fun clearFilter() {
    savedState[KEY_FILTER_TEXT] = ""
    savedState[KEY_IS_SEARCH_ACTIVE] = false
  }

  fun reload() {
    mutableIsLoading.update { true }
    reloadJob?.cancel()
    reloadJob = viewModelScope.launch {
      try {
        val tags = tagsDao.getTags().mapNotNull { it.toTagItem() }.toImmutableList()
        mutableTags.update { tags }
        mutableFailure.update { null }
        mutableIsLoading.update { false }
      } catch (e: CancellationException) {
        // a newer reload() cancelled us — leave the flows alone so it can finish
        throw e
      } catch (e: Exception) {
        logcat.e(e) { "Failed loading tags" }
        mutableFailure.update { e.requireMessage() }
        mutableIsLoading.update { false }
      }
    }
  }

  private fun buildSuccessState(
    isSearchActive: Boolean,
    tags: ImmutableList<TagItem>,
    filterText: String,
  ): Success {
    val filtered =
      if (isSearchActive) {
        tags.filter { tag -> filterText in tag }.toImmutableList()
      } else {
        tags
      }
    return Success(tags = filtered, filterText = filterText, isSearchActive = isSearchActive)
  }

  private operator fun TagItem.contains(query: String): Boolean {
    val q = query.trim()
    return tag.contains(q, ignoreCase = true) || description.contains(q, ignoreCase = true)
  }

  private companion object {
    const val KEY_FILTER_TEXT = "filter_text"
    const val KEY_IS_SEARCH_ACTIVE = "is_search_active"
  }
}
