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
import aktual.di.BudgetCoroutineScope
import aktual.di.BudgetScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.prefs.TagPreferences
import alakazam.kotlin.requireMessage
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
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
  private val budgetScope: BudgetCoroutineScope,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  private val retries = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

  private val mutableEvents =
    MutableSharedFlow<ListTagsEvent>(
      replay = 0,
      extraBufferCapacity = 1,
      onBufferOverflow = DROP_OLDEST,
    )
  val events: SharedFlow<ListTagsEvent> = mutableEvents.asSharedFlow()

  val state: StateFlow<ListTagsState> =
    retries
      .onStart { emit(Unit) }
      .flatMapLatest {
        combine(
            observeTags(),
            preferences.sortField.asFlow(),
            preferences.sortDirection.asFlow(),
          ) { tags, sortField, sortDirection ->
            if (tags.isEmpty()) {
              Empty
            } else {
              buildSuccessState(tags, TagSort(sortField, sortDirection))
            }
          }
          .onStart<ListTagsState> { emit(Loading) }
          .catch { e ->
            logcat.e(e) { "Failed loading tags" }
            emit(Failure(e.requireMessage()))
          }
      }
      .stateIn(viewModelScope, Eagerly, initialValue = Loading)

  fun retry() {
    retries.tryEmit(Unit)
  }

  // Recounts when transactions change too, since their notes hold the tag usages
  private fun observeTags(): Flow<List<TagItem>> =
    combine(tagsDao.observeTags(), transactionDao.observeChanges()) { rows, _ -> rows }
      .mapLatest { rows ->
        val counts = transactionCountsByTag()
        rows.mapNotNull { row -> row.toTagItem(counts[row.tag?.lowercase()] ?: 0) }
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
      val item = (state.value as? Success)?.tags?.firstOrNull { it.id == id }
      try {
        // syncChanges applies the tombstone to the local db and queues it for sync
        syncController.syncChanges(tombstone(dataset = TAGS, row = id.toString()))
        if (item != null) {
          mutableEvents.tryEmit(ListTagsEvent.Deleted(item))
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

  // The undo snackbar can outlive this screen, so the restore can't be tied to viewModelScope
  fun undoDelete(item: TagItem) {
    budgetScope.launch {
      try {
        // reverse the tombstone in the local db and queue the reversal for sync. The row's other
        // columns were never touched by the delete, so clearing the tombstone fully restores it
        syncController.syncChanges(untombstone(dataset = TAGS, row = item.id.toString()))
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

  private fun buildSuccessState(tags: List<TagItem>, sort: TagSort) =
    Success(tags = tags.sortedWith(sort.comparator()).toImmutableList(), sort = sort)

  private fun TagSort.comparator(): Comparator<TagItem> {
    val byField =
      when (field) {
        Name -> compareBy { it.tag.lowercase() }
        Usage -> compareBy<TagItem> { it.numTransactions }.thenBy { it.tag.lowercase() }
      }
    return direction.apply(byField)
  }
}
