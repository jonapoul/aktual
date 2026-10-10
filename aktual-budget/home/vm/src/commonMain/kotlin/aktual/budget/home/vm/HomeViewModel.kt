package aktual.budget.home.vm

import aktual.budget.BudgetLocalPreferences
import aktual.budget.model.DbMetadata
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
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import logcat.logcat

@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class HomeViewModel(
  localPreferences: BudgetLocalPreferences,
  thisMonthLoader: ThisMonthLoader,
  needsAttentionLoader: NeedsAttentionLoader,
  accountsSummaryLoader: AccountsSummaryLoader,
  upcomingSchedulesLoader: UpcomingSchedulesLoader,
) : ViewModel() {
  private var retries by mutableIntStateOf(0)

  val state: StateFlow<HomeState> =
    viewModelScope.launchMolecule(Immediate) {
      val budgetNameFlow = remember { localPreferences.observe(DbMetadata.BudgetName) }
      val budgetName by
        budgetNameFlow.collectAsState(initial = localPreferences[DbMetadata.BudgetName])

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

      HomeState(
        budgetName = budgetName,
        thisMonth = thisMonth,
        attention = attention,
        upcoming = upcoming,
        accounts = accounts,
      )
    }

  // Restarts every source. Cards that were fine keep showing what they had
  fun retry() {
    retries++
  }

  // A failing source only takes down its own card
  private fun <S> Flow<S>.orFailed(card: String, failed: S): Flow<S> = catch { e ->
    logcat.e(e) { "Failed loading $card card" }
    emit(failed)
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
        add(AttentionItem.SyncFailedMany(failedAccounts.size))
      } else {
        failedAccounts.forEach { add(AttentionItem.SyncFailed(it.id, it.name, it.status)) }
      }
      if (uncategorisedCount > 0) add(AttentionItem.Uncategorised(uncategorisedCount))
      if (overspent.isNotEmpty()) add(AttentionItem.Overspent(overspent))
      if (overdueSchedules > 0) add(AttentionItem.OverdueSchedules(overdueSchedules))
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
