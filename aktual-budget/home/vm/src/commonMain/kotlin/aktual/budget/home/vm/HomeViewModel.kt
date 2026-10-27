package aktual.budget.home.vm

import aktual.budget.BudgetLocalPreferences
import aktual.budget.model.DbMetadataKey.Companion.BudgetName
import aktual.budget.model.DbMetadataKey.Companion.HomeCardOrder
import aktual.budget.model.WidgetId
import aktual.budget.reports.vm.pinned.PinnedReport
import aktual.budget.reports.vm.pinned.PinnedReports
import aktual.budget.reports.vm.pinned.PinnedReports.Companion.MAX_PINNED_REPORTS
import aktual.budget.reports.vm.pinned.ReportPage
import aktual.core.model.unaryPlus
import aktual.di.BudgetScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class HomeViewModel
internal constructor(
  private val localPreferences: BudgetLocalPreferences,
  thisMonthLoader: ThisMonthLoader,
  needsAttentionLoader: NeedsAttentionLoader,
  accountsSummaryLoader: AccountsSummaryLoader,
  upcomingSchedulesLoader: UpcomingSchedulesLoader,
  private val pinnedReports: PinnedReports,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  private var retries by mutableIntStateOf(0)

  val state: StateFlow<HomeState> =
    viewModelScope.launchMolecule(Immediate) {
      val budgetNameFlow = remember { localPreferences.observe(BudgetName) }
      val budgetName by budgetNameFlow.collectAsState(initial = localPreferences[BudgetName])

      val cardOrderFlow = remember { localPreferences.observe(HomeCardOrder) }
      val cardOrder by cardOrderFlow.collectAsState(initial = localPreferences[HomeCardOrder])

      val thisMonthFlow =
        remember(retries) {
          thisMonthLoader.observe().map { it.toCardState() }.orFailed("this month", Failed)
        }
      val thisMonth by thisMonthFlow.collectAsState(initial = Loading)

      val attentionFlow =
        remember(retries) {
          needsAttentionLoader
            .observe()
            .map { it.toCardState() }
            .orFailed("needs attention", Failed)
        }
      val attention by attentionFlow.collectAsState(initial = Loading)

      val accountsFlow =
        remember(retries) {
          accountsSummaryLoader.observe().map { it.toCardState() }.orFailed("accounts", Failed)
        }
      val accounts by accountsFlow.collectAsState(initial = Loading)

      val upcomingFlow =
        remember(retries) {
          upcomingSchedulesLoader.observe().map { it.toCardState() }.orFailed("upcoming", Failed)
        }
      val upcoming by upcomingFlow.collectAsState(initial = Loading)

      val reportsFlow =
        remember(retries) {
          pinnedReports.observe().map { it.toCardState() }.orFailed("reports", Failed)
        }
      val reports by reportsFlow.collectAsState(initial = Loading)

      val reportChoicesFlow = remember(retries) { pinnedReports.observeChoices().orNoChoices() }
      val reportChoices by reportChoicesFlow.collectAsState(initial = persistentListOf())

      // Cards all load together rather than popping in one at a time. Retries keep the old values,
      // so this only holds them back on the first load
      val isLoading =
        thisMonth == Loading ||
          attention == Loading ||
          upcoming == Loading ||
          accounts == Loading ||
          reports == Loading

      HomeState(
        budgetName = budgetName,
        thisMonth = if (isLoading) Loading else thisMonth,
        attention = if (isLoading) Loading else attention,
        upcoming = if (isLoading) Loading else upcoming,
        accounts = if (isLoading) Loading else accounts,
        reports = if (isLoading) Loading else reports,
        reportChoices = reportChoices,
        cards = remember(cardOrder) { homeCardOrder(cardOrder) },
      )
    }

  // Restarts every source. Cards that were fine keep showing what they had
  fun retry() {
    retries++
  }

  fun swap(card: HomeCardType, other: HomeCardType) {
    localPreferences.update { meta ->
      val order = homeCardOrder(meta[HomeCardOrder]).swapping(card, other)
      meta.set(HomeCardOrder, order.map { it.name })
    }
  }

  fun reorder(cards: List<HomeCardType>) {
    localPreferences.update { meta ->
      val order = homeCardOrder(meta[HomeCardOrder]).reordering(cards)
      meta.set(HomeCardOrder, order.map { it.name })
    }
  }

  fun pinReport(id: WidgetId) {
    viewModelScope.launch { pinnedReports.pin(id) }
  }

  fun unpinReport(id: WidgetId) {
    viewModelScope.launch { pinnedReports.unpin(id) }
  }

  fun replaceReport(old: WidgetId, new: WidgetId) {
    viewModelScope.launch { pinnedReports.replace(old, new) }
  }

  // A failing source only takes down its own card
  private fun <S> Flow<S>.orFailed(card: String, failed: S): Flow<S> = catch { e ->
    logcat.e(e) { "Failed loading $card card" }
    emit(failed)
  }

  // Picking is a nicety, so a failure just leaves nothing to pick
  private fun Flow<ImmutableList<ReportPage>>.orNoChoices() = catch { e ->
    logcat.e(e) { "Failed loading report choices" }
    emit(persistentListOf())
  }

  private fun ThisMonth.toCardState(): ThisMonthCardState =
    when (val budget = budget) {
      is Envelope ->
        ThisMonthCardState.Envelope(
          month = budget.month,
          daysLeft = daysLeft,
          spent = -budget.spent,
          budgeted = budget.budgeted,
          toBudget = budget.toBudget,
        )

      is Tracking ->
        ThisMonthCardState.Tracking(
          month = budget.month,
          daysLeft = daysLeft,
          spent = -budget.spent,
          budgeted = budget.budgeted,
          income = budget.income,
          incomeBudgeted = budget.incomeBudgeted,
        )
    }

  // In priority order
  private fun NeedsAttention.toCardState(): AttentionCardState {
    val items = buildList {
      if (failedAccounts.size > MAX_SYNC_FAILURE_ROWS) {
        +AttentionItem.SyncFailedMany(failedAccounts.size)
      } else {
        failedAccounts.forEach { +AttentionItem.SyncFailed(it.id, it.name, it.status) }
      }
      if (uncategorisedCount > 0) +AttentionItem.Uncategorised(uncategorisedCount)
      if (overspent.isNotEmpty()) +AttentionItem.Overspent(overspent)
      if (overdueSchedules > 0) +AttentionItem.OverdueSchedules(overdueSchedules)
    }
    return if (items.isEmpty()) Empty else AttentionCardState.Loaded(items.toPersistentList())
  }

  private fun UpcomingSchedules.toCardState(): UpcomingCardState =
    if (schedules.isEmpty()) {
      Empty
    } else {
      UpcomingCardState.Loaded(
        length = length,
        today = today,
        schedules = schedules.take(MAX_UPCOMING_ROWS).toImmutableList(),
        hiddenCount = (schedules.size - MAX_UPCOMING_ROWS).coerceAtLeast(0),
        total = total,
      )
    }

  private fun List<PinnedReport>.toCardState(): ReportsCardState =
    if (isEmpty()) {
      Empty
    } else {
      ReportsCardState.Loaded(toImmutableList(), canPinMore = size < MAX_PINNED_REPORTS)
    }

  // Closed accounts aren't shown on the card
  private fun AccountsSummary.toCardState(): AccountsCardState =
    if (onBudget.accounts.isEmpty() && offBudget.accounts.isEmpty()) {
      Empty
    } else {
      AccountsCardState.Loaded(this, recent = mostRecentlyActive(MAX_COLLAPSED_ACCOUNTS))
    }
}

private const val MAX_UPCOMING_ROWS = 5
private const val MAX_SYNC_FAILURE_ROWS = 3
private const val MAX_COLLAPSED_ACCOUNTS = 5
