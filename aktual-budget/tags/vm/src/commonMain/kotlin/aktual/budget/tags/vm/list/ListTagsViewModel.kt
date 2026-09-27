package aktual.budget.tags.vm.list

import aktual.budget.BudgetSyncController
import aktual.budget.db.dao.DatabaseTables.TAGS
import aktual.budget.db.dao.TagsDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.model.TagId
import aktual.budget.model.TagSort
import aktual.budget.model.tagsInNotes
import aktual.budget.model.tombstone
import aktual.budget.model.untombstone
import aktual.di.BudgetScope
import aktual.prefs.TagPreferences
import alakazam.kotlin.requireMessage
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class ListTagsViewModel(
  private val tagsDao: TagsDao,
  private val transactionDao: TransactionDao,
  private val syncController: BudgetSyncController,
  private val preferences: TagPreferences,
) : ViewModel() {
  private val mutableTags = MutableStateFlow<ImmutableList<TagItem>>(persistentListOf())
  private val mutableIsLoading = MutableStateFlow(true)
  private val mutableIsRefreshing = MutableStateFlow(false)
  private val mutableFailure = MutableStateFlow<String?>(null)
  private var reloadJob: Job? = null

  private val mutableEvents =
    MutableSharedFlow<ListTagsEvent>(
      replay = 0,
      extraBufferCapacity = 1,
      onBufferOverflow = DROP_OLDEST,
    )
  val events: SharedFlow<ListTagsEvent> = mutableEvents.asSharedFlow()

  val isRefreshing: StateFlow<Boolean> = mutableIsRefreshing.asStateFlow()

  val state: StateFlow<ListTagsState> =
    viewModelScope.launchMolecule(Immediate) {
      val tags by mutableTags.collectAsState()
      val isLoading by mutableIsLoading.collectAsState()
      val failure by mutableFailure.collectAsState()
      val sortField by preferences.sortField.asFlow().collectAsState(TagSort.Field.Default)
      val sortDirection by
        preferences.sortDirection.asFlow().collectAsState(TagSort.Direction.Default)
      @Suppress("BracesOnWhenStatements")
      when {
        isLoading -> Loading
        failure != null -> Failure(failure)
        tags.isEmpty() -> Empty
        else -> buildSuccessState(tags, TagSort(sortField, sortDirection))
      }
    }

  init {
    reload()
  }

  fun reload(showLoading: Boolean = true) = load(loading = showLoading, refreshing = false)

  fun refresh() = load(loading = false, refreshing = true)

  private fun load(loading: Boolean, refreshing: Boolean) {
    mutableIsLoading.update { loading }
    mutableIsRefreshing.update { refreshing }
    reloadJob?.cancel()
    reloadJob = viewModelScope.launch {
      try {
        val counts = transactionCountsByTag()
        val tags =
          tagsDao
            .getTags()
            .mapNotNull { row -> row.toTagItem(counts[row.tag?.lowercase()] ?: 0) }
            .toImmutableList()
        mutableTags.update { tags }
        mutableFailure.update { null }
      } catch (e: CancellationException) {
        // a newer load() cancelled us - leave the flows alone so it can finish
        throw e
      } catch (e: Exception) {
        logcat.e(e) { "Failed loading tags" }
        mutableFailure.update { e.requireMessage() }
      } finally {
        mutableIsLoading.update { false }
        mutableIsRefreshing.update { false }
      }
    }
  }

  // Counts, per lowercased tag name, how many transactions reference that tag in their notes.
  // A single transaction counts once even if it mentions the tag multiple times.
  private suspend fun transactionCountsByTag(): Map<String, Int> {
    val counts = HashMap<String, Int>()
    for (notes in transactionDao.getNotesContainingHash()) {
      for (name in tagsInNotes(notes)) {
        counts[name] = (counts[name] ?: 0) + 1
      }
    }
    return counts
  }

  fun delete(id: TagId) {
    viewModelScope.launch {
      val index = mutableTags.value.indexOfFirst { it.id == id }
      val item = mutableTags.value.getOrNull(index)
      try {
        // syncChanges applies the tombstone to the local db and queues it for sync
        syncController.syncChanges(tombstone(dataset = TAGS, row = id.toString()))
        // optimistically drop the row so it animates out without a loading flash
        mutableTags.update { tags -> tags.filterNot { it.id == id }.toImmutableList() }
        if (item != null) {
          mutableEvents.tryEmit(ListTagsEvent.Deleted(item, index))
        }
        logcat.d { "Tombstoned tag $id" }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.e(e) { "Failed deleting tag $id" }
        mutableEvents.tryEmit(ListTagsEvent.DeleteFailed(item?.tag))
      }
    }
  }

  fun undoDelete(item: TagItem, index: Int) {
    viewModelScope.launch {
      try {
        // reverse the tombstone in the local db and queue the reversal for sync. The row's other
        // columns were never touched by the delete, so clearing the tombstone fully restores it
        syncController.syncChanges(untombstone(dataset = TAGS, row = item.id.toString()))
        // optimistically slot the row back where it was so it animates in
        mutableTags.update { tags ->
          if (tags.any { it.id == item.id }) {
            tags
          } else {
            tags.toMutableList().apply { add(index.coerceIn(0, size), item) }.toImmutableList()
          }
        }
        logcat.d { "Restored tag ${item.id}" }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.e(e) { "Failed restoring tag ${item.id}" }
        mutableEvents.tryEmit(ListTagsEvent.RestoreFailed(item.tag))
      }
    }
  }

  fun setSort(sort: TagSort) {
    viewModelScope.launch {
      preferences.sortField.set(sort.field)
      preferences.sortDirection.set(sort.direction)
    }
  }

  private fun buildSuccessState(tags: ImmutableList<TagItem>, sort: TagSort) =
    Success(tags = tags.sortedWith(sort.comparator()).toImmutableList(), sort = sort)

  private fun TagSort.comparator(): Comparator<TagItem> {
    val byField =
      when (field) {
        Name -> compareBy { it.tag.lowercase() }
        Usage -> compareBy<TagItem> { it.numTransactions }.thenBy { it.tag.lowercase() }
      }
    return when (direction) {
      Ascending -> byField
      Descending -> byField.reversed()
    }
  }
}
