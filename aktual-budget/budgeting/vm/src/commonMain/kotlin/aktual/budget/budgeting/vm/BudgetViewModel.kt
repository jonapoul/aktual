package aktual.budget.budgeting.vm

import aktual.budget.BudgetLocalPreferences
import aktual.budget.BudgetSyncController
import aktual.budget.SyncStateHolder
import aktual.budget.budgeting.domain.BudgetMonth
import aktual.budget.budgeting.domain.BudgetMonthCalculator
import aktual.budget.budgeting.domain.CategoryGroupMonth
import aktual.budget.budgeting.domain.CategoryMonth
import aktual.budget.db.dao.TransactionDao
import aktual.budget.model.Amount
import aktual.budget.model.BudgetType
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.DbMetadata
import aktual.budget.model.DbMetadata.Companion.BudgetCollapsed
import aktual.budget.model.DbMetadata.Companion.BudgetShowHiddenCategories
import aktual.budget.model.DbMetadata.Companion.MobileShowSpentColumn
import aktual.core.Calendar
import aktual.di.BudgetScope
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.datetime.YearMonth
import kotlinx.datetime.yearMonth
import logcat.logcat

// packages/desktop-client/src/components/mobile/budget/BudgetPage.tsx
@Stable
@AssistedInject
class BudgetViewModel(
  @Assisted private val month: YearMonth?,
  private val calculator: BudgetMonthCalculator,
  private val transactionDao: TransactionDao,
  private val localPreferences: BudgetLocalPreferences,
  private val syncController: BudgetSyncController,
  private val calendar: Calendar,
  syncStateHolder: SyncStateHolder,
) : ViewModel() {
  private var retries by mutableIntStateOf(0)

  val state: StateFlow<BudgetState> =
    viewModelScope.launchMolecule(Immediate) {
      val metadata by localPreferences.collectAsState()
      val sourceFlow = remember(retries) { observeSource() }
      val source by sourceFlow.collectAsState(initial = Source.Loading)

      when (val current = source) {
        Loading -> BudgetState.Loading
        Failed -> BudgetState.Failed
        is Data -> current.toState(metadata)
      }
    }

  val isSyncing: StateFlow<Boolean> =
    syncStateHolder.map { it == Syncing }.stateIn(viewModelScope, Eagerly, initialValue = false)

  fun refresh() = syncController.schedule()

  fun retry() {
    retries++
  }

  fun toggleSpent() = localPreferences.update { it.toggle(MobileShowSpentColumn) }

  fun toggleHidden() = localPreferences.update { it.toggle(BudgetShowHiddenCategories) }

  fun toggleCollapsed(id: CategoryGroupId) = localPreferences.update { metadata ->
    val collapsed = metadata[BudgetCollapsed].orEmpty()
    metadata.set(
      BudgetCollapsed,
      if (id.value in collapsed) collapsed - id.value else collapsed + id.value,
    )
  }

  // Without a month in the route, follow the current one
  private fun observeSource(): Flow<Source> =
    calendar
      .observeToday()
      .map { it.yearMonth }
      .distinctUntilChanged()
      .flatMapLatest { current ->
        combine(
          calculator.observe(month ?: current),
          transactionDao.observeUncategorisedCount(),
        ) { budget, uncategorised ->
          Source.Data(budget, current, uncategorised.toInt())
        }
      }
      .catch<Source> { e ->
        logcat.e(e) { "Failed loading budget for $month" }
        emit(Failed)
      }

  private sealed interface Source {
    data object Loading : Source

    data object Failed : Source

    data class Data(val budget: BudgetMonth, val current: YearMonth, val uncategorised: Int) :
      Source
  }

  private fun Source.Data.toState(metadata: DbMetadata): BudgetState.Loaded {
    val showHidden = metadata[BudgetShowHiddenCategories] == true
    val collapsed = metadata[BudgetCollapsed].orEmpty().toSet()
    val visible = budget.groups.filter { showHidden || !it.isHidden }
    return BudgetState.Loaded(
      type = budget.type,
      month = budget.month,
      summary = budget.summary(current),
      groups =
        visible.filter { !it.isIncome }.map { it.toRow(collapsed, showHidden) }.toImmutableList(),
      income = visible.firstOrNull { it.isIncome }?.toRow(collapsed, showHidden),
      banners = banners(),
      showSpent = metadata[MobileShowSpentColumn] == true,
      showHidden = showHidden,
    )
  }

  private fun Source.Data.banners(): ImmutableList<Banner> = buildList {
    if (uncategorised > 0) add(Banner.Uncategorised(uncategorised))
    val overspent = budget.overspentCategories()
    if (overspent.isNotEmpty()) {
      add(Banner.Overspent(count = overspent.size, total = overspent.sumOf { it.balance }))
    }
    if (budget is Envelope && budget.toBudget < Amount.Zero) {
      add(Banner.Overbudgeted(budget.toBudget))
    }
  }
    .toImmutableList()

  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(BudgetScope::class)
  interface Factory : ManualViewModelAssistedFactory {
    fun create(month: YearMonth?): BudgetViewModel
  }
}

private val BudgetMonth.type: BudgetType
  get() =
    when (this) {
      is Envelope -> BudgetType.Envelope
      is Tracking -> BudgetType.Tracking
    }

private fun BudgetMonth.summary(current: YearMonth): BudgetSummary =
  when (this) {
    is Envelope -> {
      BudgetSummary.Envelope(toBudget = toBudget, available = availableFunds, budgeted = budgeted)
    }

    is Tracking -> {
      val isProjected = month >= current
      BudgetSummary.Tracking(
        // tracking.ts total-saved and real-saved
        saved = if (isProjected) incomeBudgeted - budgeted else income + spent,
        isProjected = isProjected,
        budgeted = budgeted,
        spent = spent,
      )
    }
  }

// packages/desktop-client/src/hooks/useOverspentCategories.ts. Overspending that rolls over isn't
// flagged, and tracking budgets leave out hidden categories
private fun BudgetMonth.overspentCategories(): List<CategoryMonth> {
  val hiddenGroups = groups.filter { it.isHidden }.map { it.id }.toSet()
  return categories.filter { category ->
    !category.isIncome &&
      !category.carryover &&
      category.balance < Amount.Zero &&
      (this is Envelope || !category.isHidden && category.group !in hiddenGroups)
  }
}

private fun CategoryGroupMonth.toRow(collapsed: Set<String>, showHidden: Boolean) =
  GroupRow(
    id = id,
    name = name,
    isHidden = isHidden,
    isCollapsed = id.value in collapsed,
    budgeted = budgeted,
    spent = spent,
    balance = balance,
    categories =
      categories
        .filter { showHidden || !it.isHidden }
        .map { it.toRow(isGroupHidden = isHidden) }
        .toImmutableList(),
  )

private fun CategoryMonth.toRow(isGroupHidden: Boolean) =
  CategoryRow(
    id = id,
    name = name,
    isHidden = isHidden || isGroupHidden,
    budgeted = budgeted,
    spent = spent,
    balance = balance,
    carryover = carryover,
  )

private fun DbMetadata.toggle(key: DbMetadata.Key<Boolean>): DbMetadata =
  set(key, this[key] != true)

private inline fun <T> Iterable<T>.sumOf(selector: (T) -> Amount): Amount =
  fold(Amount.Zero) { sum, item -> sum + selector(item) }
