package aktual.budget.schedules.vm.list

import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.ScheduleDao
import aktual.budget.db.schedules.GetAllActive
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.AmountOperator
import aktual.budget.model.Field
import aktual.budget.model.Operator
import aktual.budget.model.PayeeId
import aktual.budget.model.RecurConfig
import aktual.budget.model.ScheduleId
import aktual.budget.model.UpcomingLength
import aktual.budget.model.upcomingDays
import aktual.budget.schedules.vm.Schedule
import aktual.budget.schedules.vm.ScheduleStatus
import aktual.core.model.Calendar
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit.Companion.DAY
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import logcat.logcat

@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class ListSchedulesViewModel(
  private val scheduleDao: ScheduleDao,
  private val accountDao: AccountDao,
  private val payeeDao: PayeeDao,
  private val calendar: Calendar,
) : ViewModel() {
  private val mutableSchedules = MutableStateFlow<ImmutableList<Schedule>>(persistentListOf())
  private val mutableIsLoading = MutableStateFlow(true)
  private val mutableFailure = MutableStateFlow<String?>(null)
  private val mutableFilterText = MutableStateFlow("")
  private val mutableIsSearchActive = MutableStateFlow(false)

  val state: StateFlow<ListSchedulesState> =
    viewModelScope.launchMolecule(Immediate) {
      val schedules by mutableSchedules.collectAsState()
      val isLoading by mutableIsLoading.collectAsState()
      val failure by mutableFailure.collectAsState()
      val filterText by mutableFilterText.collectAsState()
      val isSearchActive by mutableIsSearchActive.collectAsState()
      when {
        isLoading -> Loading
        failure != null -> Failure(failure)
        schedules.isEmpty() -> Empty
        else -> {
          val filtered =
            if (isSearchActive) {
              schedules.filter { s -> filterText in s }.toImmutableList()
            } else {
              schedules
            }
          Success(schedules = filtered, filterText = filterText, isSearchActive = isSearchActive)
        }
      }
    }

  init {
    reload()
  }

  fun openSearch() {
    mutableIsSearchActive.update { true }
  }

  fun setFilterText(text: String) {
    mutableFilterText.update { text }
  }

  fun clearFilter() {
    mutableFilterText.update { "" }
    mutableIsSearchActive.update { false }
  }

  fun reload() {
    mutableIsLoading.update { true }
    viewModelScope.launch {
      try {
        val today = calendar.today()
        val rows = scheduleDao.getAll()

        val payeeNames = payeeDao.getAllActive().associate { it.id to it.name }
        val accountNames = accountDao.nameMap()

        // Fetch transactions for all schedules in one query using the earliest possible fromDate
        val minFromDate =
          rows.mapNotNull { it.next_date }.minOrNull()?.minus(value = 2, unit = DAY) ?: today
        val latestTxDates = scheduleDao.latestTransactionDates(minFromDate)

        val schedules =
          rows
            .mapNotNull { row -> toSchedule(row, today, payeeNames, accountNames, latestTxDates) }
            .sortedWith(compareBy({ it.nextDate }, { it.status.ordinal }))
            .toImmutableList()
        mutableSchedules.update { schedules }
        mutableFailure.update { null }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.e(e) { "Failed loading schedules" }
        mutableFailure.update { e.requireMessage() }
      } finally {
        mutableIsLoading.update { false }
      }
    }
  }

  @Suppress("ReturnCount")
  private fun toSchedule(
    row: GetAllActive,
    today: LocalDate,
    payeeNames: Map<PayeeId, String?>,
    accountNames: Map<AccountId, String?>,
    latestTxDates: Map<ScheduleId, LocalDate>,
  ): Schedule? {
    val nextDate = row.next_date ?: return null
    val ruleId = row.rule ?: return null
    val payeeId = row._payee ?: return null
    val accountId = row._account?.let { AccountId(it) } ?: return null

    val payeeName = payeeNames[payeeId] ?: return null
    val accountName = accountNames[accountId] ?: ""

    val amount = row._amount?.toLongOrNull()?.let { Amount(it) } ?: Amount.Zero
    val amountOp =
      row._amountOp?.let { runCatching { Operator.parse(it) as? AmountOperator }.getOrNull() }
        ?: Operator.IsApprox

    // Fixed date (op = 'is'): check from next_date; recurring: look back 2 days
    val dateCondOp = row._conditions?.firstOrNull { it.field == Field.Date }?.operator
    val txFromDate =
      if (dateCondOp == Operator.Is) nextDate else nextDate.minus(value = 2, unit = DAY)
    val hasTransaction = latestTxDates[row.id]?.let { it >= txFromDate } ?: false

    val customUpcomingLength = row.custom_upcoming_length

    return Schedule(
      id = row.id,
      name = row.name,
      ruleId = ruleId,
      nextDate = nextDate,
      isCompleted = row.completed == true,
      postsTransaction = row.posts_transaction == true,
      customUpcomingLength = customUpcomingLength,
      payeeId = payeeId,
      payeeName = payeeName,
      accountId = accountId,
      accountName = accountName,
      amount = amount,
      amountOp = amountOp,
      date = parseDateField(row._date, nextDate),
      status =
        scheduleStatus(
          nextDate = nextDate,
          isCompleted = row.completed == true,
          hasTransaction = hasTransaction,
          customUpcomingLength = customUpcomingLength,
          today = today,
        ),
    )
  }

  // Mirrors getStatus() in packages/loot-core/src/shared/schedules.ts
  private fun scheduleStatus(
    nextDate: LocalDate,
    isCompleted: Boolean,
    hasTransaction: Boolean,
    customUpcomingLength: UpcomingLength?,
    today: LocalDate,
  ): ScheduleStatus {
    val length = customUpcomingLength ?: UpcomingLength.Days(7)
    val upcomingDays = length.upcomingDays(today)
    return when {
      isCompleted -> ScheduleStatus.Completed
      hasTransaction -> ScheduleStatus.Paid
      nextDate == today -> ScheduleStatus.Due
      nextDate > today && nextDate <= today.plus(value = upcomingDays, unit = DAY) ->
        ScheduleStatus.Upcoming
      nextDate < today -> ScheduleStatus.Missed
      else -> ScheduleStatus.Scheduled
    }
  }

  // _date is either a plain "YYYY-MM-DD" or a RecurConfig JSON object {"start":"YYYY-MM-DD",...}
  private fun parseDateField(raw: String?, fallback: LocalDate): LocalDate {
    raw ?: return fallback
    return when (val element = Json.parseToJsonElement(raw)) {
      is JsonNull,
      is JsonArray -> {
        logcat.e { "Got unexpected JSON data format in $element" }
        fallback
      }

      is JsonObject -> {
        Json.decodeFromJsonElement(RecurConfig.serializer(), element).start
      }

      is JsonPrimitive -> {
        LocalDate.parse(element.content)
      }
    }
  }

  private operator fun Schedule.contains(query: String): Boolean {
    val q = query.trim()
    return name?.contains(q, ignoreCase = true) == true ||
      payeeName.contains(q, ignoreCase = true) ||
      accountName.contains(q, ignoreCase = true)
  }
}
