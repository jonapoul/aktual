package aktual.budget.budgeting.vm

import aktual.budget.budgeting.domain.CategoryOrder
import aktual.budget.budgeting.domain.OrderedCategory
import aktual.budget.budgeting.domain.OrderedGroup
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import aktual.core.utils.runCatchingCancellable
import aktual.di.BudgetScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

// The order is only written when saved. Income groups stay below the expense ones, and a category
// can't move between expense and income groups, as upstream's mobile budget page allows
@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class ReorderCategoriesViewModel
internal constructor(
  private val order: CategoryOrder,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  private val mutableState = MutableStateFlow<ReorderCategoriesState>(Loading)
  val state: StateFlow<ReorderCategoriesState> = mutableState.asStateFlow()

  private val mutableEvents = MutableSharedFlow<ReorderCategoriesEvent>(extraBufferCapacity = 1)
  val events: SharedFlow<ReorderCategoriesEvent> = mutableEvents.asSharedFlow()

  private var saved: ImmutableList<ReorderGroup>? = null

  init {
    viewModelScope.launch {
      runCatchingCancellable(
        attempt = {
          val groups = order.load().map { it.toReorderGroup() }.toImmutableList()
          saved = groups
          mutableState.update {
            ReorderCategoriesState.Loaded(groups, hasChanges = false, isSaving = false)
          }
        },
        onFailure = { e ->
          logcat.e(e) { "Failed loading category order" }
          mutableState.update { Failed }
        },
      )
    }
  }

  // Index is the category's place in the group once it's moved there
  fun moveCategory(id: CategoryId, group: CategoryGroupId, index: Int) = edit { groups ->
    val from = groups.firstOrNull { g -> g.categories.any { it.id == id } } ?: return@edit groups
    val to = groups.firstOrNull { it.id == group } ?: return@edit groups
    if (from.isIncome != to.isIncome) return@edit groups
    val category = from.categories.first { it.id == id }
    groups.map { g ->
      var categories = g.categories.filter { it.id != id }
      if (g.id == group) {
        categories =
          categories.toMutableList().apply { add(index.coerceIn(0, categories.size), category) }
      }
      g.copy(categories = categories.toImmutableList())
    }
  }

  // Index is among the expense groups
  fun moveGroup(id: CategoryGroupId, index: Int) = edit { groups ->
    val expenses = groups.filter { !it.isIncome }
    val group = expenses.firstOrNull { it.id == id } ?: return@edit groups
    val others = expenses.filter { it.id != id }
    val moved = others.toMutableList().apply { add(index.coerceIn(0, others.size), group) }
    moved + groups.filter { it.isIncome }
  }

  fun discard() {
    val saved = saved ?: return
    mutableState.update { state ->
      if (state is Loaded) state.copy(groups = saved, hasChanges = false) else state
    }
  }

  fun save() {
    val loaded = mutableState.value as? Loaded ?: return
    if (!loaded.hasChanges || loaded.isSaving) return
    mutableState.update { loaded.copy(isSaving = true) }
    viewModelScope.launch {
      runCatchingCancellable(
        attempt = {
          order.save(loaded.groups.map { it.toOrderedGroup() })
          saved = loaded.groups
          mutableState.update { state ->
            if (state is Loaded) state.copy(hasChanges = false, isSaving = false) else state
          }
          mutableEvents.emit(Saved)
        },
        onFailure = { e ->
          logcat.e(e) { "Failed saving category order" }
          mutableState.update { state ->
            if (state is Loaded) state.copy(isSaving = false) else state
          }
          mutableEvents.emit(SaveFailed)
        },
      )
    }
  }

  private fun edit(block: (List<ReorderGroup>) -> List<ReorderGroup>) {
    mutableState.update { state ->
      if (state !is Loaded || state.isSaving) return@update state
      val groups = block(state.groups).toImmutableList()
      state.copy(groups = groups, hasChanges = groups != saved)
    }
  }

  private fun OrderedGroup.toReorderGroup() =
    ReorderGroup(
      id = id,
      name = name,
      isIncome = isIncome,
      isHidden = isHidden,
      categories =
        categories.map { ReorderCategory(it.id, it.name, it.isHidden) }.toImmutableList(),
    )

  private fun ReorderGroup.toOrderedGroup() =
    OrderedGroup(
      id = id,
      name = name,
      isIncome = isIncome,
      isHidden = isHidden,
      categories = categories.map { OrderedCategory(it.id, it.name, it.isHidden) },
    )
}
