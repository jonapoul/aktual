package aktual.budget.tags.vm.list

import aktual.budget.db.dao.TagsDao
import aktual.di.BudgetScope
import alakazam.kotlin.requireMessage
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionMode.Immediate
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
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
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class ListTagsViewModel(private val tagsDao: TagsDao) : ViewModel() {
  private val mutableTags = MutableStateFlow<ImmutableList<TagItem>>(persistentListOf())
  private val mutableIsLoading = MutableStateFlow(true)
  private val mutableFailure = MutableStateFlow<String?>(null)
  private var reloadJob: Job? = null

  val state: StateFlow<ListTagsState> =
    viewModelScope.launchMolecule(Immediate) {
      val tags by mutableTags.collectAsState()
      val isLoading by mutableIsLoading.collectAsState()
      val failure by mutableFailure.collectAsState()
      when {
        isLoading -> Loading
        failure != null -> Failure(failure)
        else -> Success(tags)
      }
    }

  init {
    reload()
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
}
