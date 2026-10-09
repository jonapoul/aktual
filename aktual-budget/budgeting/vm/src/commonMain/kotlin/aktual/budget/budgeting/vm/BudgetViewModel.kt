package aktual.budget.budgeting.vm

import aktual.budget.BudgetLocalPreferences
import aktual.budget.BudgetSyncController
import aktual.budget.SyncStateHolder
import aktual.budget.budgeting.domain.BudgetMonth
import aktual.budget.budgeting.domain.BudgetMonthCalculator
import aktual.budget.budgeting.domain.BudgetWriter
import aktual.budget.budgeting.domain.CategoryGroupMonth
import aktual.budget.budgeting.domain.CategoryMonth
import aktual.budget.db.dao.TransactionDao
import aktual.budget.model.Amount
import aktual.budget.model.BudgetType
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import aktual.budget.model.DbMetadata
import aktual.budget.model.DbMetadata.Companion.BudgetCollapsed
import aktual.budget.model.DbMetadata.Companion.BudgetMonthCount
import aktual.budget.model.DbMetadata.Companion.BudgetShowHiddenCategories
import aktual.budget.model.DbMetadata.Companion.MobileShowSpentColumn
import aktual.budget.model.evaluateAmountInput
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.yearMonth
import logcat.logcat

// packages/desktop-client/src/components/mobile/budget/BudgetPage.tsx, and
// packages/desktop-client/src/components/budget/DynamicBudgetTable.tsx for several months
@Stable
@Suppress("TooManyFunctions") // One per budget write upstream offers
@AssistedInject
class BudgetViewModel(
  @Assisted private val month: YearMonth?,
  private val calculator: BudgetMonthCalculator,
  private val writer: BudgetWriter,
  private val transactionDao: TransactionDao,
  private val localPreferences: BudgetLocalPreferences,
  private val syncController: BudgetSyncController,
  private val calendar: Calendar,
  syncStateHolder: SyncStateHolder,
) : ViewModel() {
  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(BudgetScope::class)
  interface Factory : ManualViewModelAssistedFactory {
    fun create(month: YearMonth?): BudgetViewModel
  }

  private var retries by mutableIntStateOf(0)

  // Without a month, follow the current one
  private val requested = MutableStateFlow(month)
  private val fittingMonths = MutableStateFlow(1)

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

  private val loaded: BudgetState.Loaded?
    get() = state.value as? BudgetState.Loaded

  fun refresh() = syncController.schedule()

  fun retry() {
    retries++
  }

  fun showMonth(month: YearMonth) {
    if (loaded?.month != month) requested.update { month }
  }

  fun previousMonth() = shiftMonth(by = -1)

  fun nextMonth() = shiftMonth(by = 1)

  fun showToday() = requested.update { null }

  fun setMonthCount(count: Int) = localPreferences.update {
    it.set(BudgetMonthCount, count.coerceIn(1, MAX_MONTH_COUNT))
  }

  // How many month columns the screen has room for
  fun setFittingMonths(count: Int) = fittingMonths.update { count.coerceIn(1, MAX_MONTH_COUNT) }

  fun toggleSpent() = localPreferences.update { it.toggle(MobileShowSpentColumn) }

  fun toggleHidden() = localPreferences.update { it.toggle(BudgetShowHiddenCategories) }

  fun toggleCollapsed(id: CategoryGroupId) = localPreferences.update { metadata ->
    val collapsed = metadata[BudgetCollapsed].orEmpty()
    metadata.set(
      BudgetCollapsed,
      if (id.value in collapsed) collapsed - id.value else collapsed + id.value,
    )
  }

  // Typed amounts can be arithmetic, as upstream. False if the input doesn't read as an amount
  fun setBudget(month: YearMonth, category: CategoryId, input: String): Boolean {
    val amount = evaluateAmountInput(input) ?: return false
    write("set budget") { setBudget(month, category, amount) }
    return true
  }

  fun copyLastMonth(month: YearMonth, category: CategoryId) =
    write("copy last month") { copySinglePreviousMonth(month, category) }

  fun setAverage(month: YearMonth, category: CategoryId, months: Int) =
    write("set $months month average") { setSingleAverage(month, category, months) }

  fun copyToYearEnd(month: YearMonth, category: CategoryId) =
    write("copy to year end") { copyUntilYearEnd(month, category) }

  // Typed amounts below are unsigned amounts of money to move. False if the input doesn't read as
  // one, so the sheet stays open

  // A null destination is To Budget
  fun transfer(month: YearMonth, input: String, from: CategoryId, to: CategoryId?): Boolean =
    writeAmount(input, "transfer") { amount -> transferCategory(month, amount, from, to) }

  // A null source is To Budget
  fun coverOverspending(month: YearMonth, to: CategoryId, from: CategoryId?, input: String) =
    writeAmount(input, "cover overspending") { amount ->
      coverOverspending(month, to, from, amount)
    }

  fun setCarryover(month: YearMonth, category: CategoryId, enabled: Boolean) =
    write("set carryover") { setCarryover(month, category, enabled) }

  fun hold(month: YearMonth, input: String): Boolean =
    writeAmount(input, "hold") { amount -> holdForNextMonth(month, amount) }

  fun resetHold(month: YearMonth) = write("reset hold") { resetHold(month) }

  fun transferAvailable(month: YearMonth, input: String, category: CategoryId): Boolean =
    writeAmount(input, "transfer available") { amount ->
      transferAvailable(month, amount, category)
    }

  fun coverOverbudgeted(month: YearMonth, category: CategoryId, input: String): Boolean =
    writeAmount(input, "cover overbudgeted") { amount ->
      coverOverbudgeted(month, category, amount)
    }

  private fun writeAmount(
    input: String,
    description: String,
    action: suspend BudgetWriter.(Amount) -> Unit,
  ): Boolean {
    val amount = evaluateAmountInput(input)?.takeIf { it > Amount.Zero } ?: return false
    write(description) { action(amount) }
    return true
  }

  // The calculator picks up the change, so there's nothing to update here
  private fun write(description: String, action: suspend BudgetWriter.() -> Unit) {
    viewModelScope.launch {
      try {
        writer.action()
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.e(e) { "Failed to $description" }
      }
    }
  }

  private fun shiftMonth(by: Int) {
    val month = loaded?.month ?: return
    requested.update { month.plus(by, MONTH) }
  }

  // The month changes straight away, keeping the months already loaded until the new ones arrive
  private fun observeSource(): Flow<Source> =
    observeWindow()
      .transformLatest { window ->
        emit(Update.Moved(window))
        emitAll(
          combine(
            calculator.observeRange(window.prefetched),
            transactionDao.observeUncategorisedCount(),
          ) { months, uncategorised ->
            Update.Fetched(Source.Data(window, months, uncategorised.toInt()))
          },
        )
      }
      .scan<Update, Source>(Loading) { previous, update ->
        when (update) {
          is Moved -> (previous as? Source.Data)?.copy(window = update.window) ?: previous
          is Fetched -> update.data
        }
      }
      .catch { e ->
        logcat.e(e) { "Failed loading budget for $month" }
        emit(Failed)
      }

  private fun observeWindow(): Flow<Window> =
    combine(
        calendar.observeToday().map { it.yearMonth }.distinctUntilChanged(),
        calculator.observeBounds(),
        requested,
        localPreferences.observe(BudgetMonthCount).distinctUntilChanged(),
        fittingMonths,
      ) { current, bounds, requested, chosen, fitting ->
        // packages/desktop-client/src/components/budget/index.tsx maxMonths
        val count = minOf(chosen ?: 1, fitting).coerceIn(1, MAX_MONTH_COUNT)
        val lastStart = maxOf(bounds.start, bounds.endInclusive.minus(count - 1, MONTH))
        Window(
          first = (requested ?: current).coerceIn(bounds.start, lastStart),
          count = count,
          fitting = fitting,
          current = current,
          earliest = bounds.start,
          latest = bounds.endInclusive,
        )
      }
      .distinctUntilChanged()

  private data class Window(
    val first: YearMonth,
    val count: Int,
    val fitting: Int,
    val current: YearMonth,
    val earliest: YearMonth,
    val latest: YearMonth,
  ) {
    // A month either side is loaded ahead of paging to it
    val prefetched: ClosedRange<YearMonth>
      get() =
        maxOf(earliest, first.minus(1, MONTH))..minOf(
            latest,
            first.plus(count, MONTH),
          )
  }

  private sealed interface Update {
    data class Moved(val window: Window) : Update

    data class Fetched(val data: Source.Data) : Update
  }

  private sealed interface Source {
    data object Loading : Source

    data object Failed : Source

    data class Data(
      val window: Window,
      val months: ImmutableList<BudgetMonth>,
      val uncategorised: Int,
    ) : Source
  }

  private fun Source.Data.toState(metadata: DbMetadata): BudgetState.Loaded {
    val showHidden = metadata[BudgetShowHiddenCategories] == true
    val collapsed = metadata[BudgetCollapsed].orEmpty().toSet()
    return BudgetState.Loaded(
      type = months.firstOrNull()?.type() ?: Envelope,
      month = window.first,
      current = window.current,
      earliest = window.earliest,
      latest = window.latest,
      monthCount = window.count,
      maxMonthCount = window.fitting,
      months =
        months
          .map { it.toMonthBudget(window.current, uncategorised, collapsed, showHidden) }
          .toImmutableList(),
      showSpent = metadata[MobileShowSpentColumn] == true,
      showHidden = showHidden,
    )
  }

  private fun BudgetMonth.toMonthBudget(
    current: YearMonth,
    uncategorised: Int,
    collapsed: Set<String>,
    showHidden: Boolean,
  ): MonthBudget {
    val visible = groups.filter { showHidden || !it.isHidden }
    return MonthBudget(
      month = month,
      summary = summary(current),
      groups =
        visible.filter { !it.isIncome }.map { it.toRow(collapsed, showHidden) }.toImmutableList(),
      income = visible.firstOrNull { it.isIncome }?.toRow(collapsed, showHidden),
      banners = banners(uncategorised),
    )
  }

  private fun BudgetMonth.banners(uncategorised: Int): ImmutableList<Banner> = buildList {
    if (uncategorised > 0) add(Banner.Uncategorised(uncategorised))
    val overspent = overspentCategories()
    if (overspent.isNotEmpty()) {
      add(Banner.Overspent(count = overspent.size, total = overspent.sumOf { it.balance }))
    }
    if (this@banners is Envelope && toBudget < Amount.Zero) {
      add(Banner.Overbudgeted(toBudget))
    }
  }
    .toImmutableList()

  private fun BudgetMonth.type(): BudgetType =
    when (this) {
      is Envelope -> BudgetType.Envelope
      is Tracking -> BudgetType.Tracking
    }

  private fun BudgetMonth.summary(current: YearMonth): BudgetSummary =
    when (this) {
      is Envelope -> {
        BudgetSummary.Envelope(
          toBudget = toBudget,
          available = availableFunds,
          budgeted = budgeted,
          overspentLastMonth = lastMonthOverspent,
          held = buffered,
        )
      }

      is Tracking -> {
        val isProjected = month >= current
        BudgetSummary.Tracking(
          // tracking.ts total-saved and real-saved
          saved = if (isProjected) incomeBudgeted - budgeted else income + spent,
          isProjected = isProjected,
          budgeted = budgeted,
          spent = spent,
          incomeBudgeted = incomeBudgeted,
          received = income,
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

  private companion object {
    const val MAX_MONTH_COUNT = 4
  }
}
