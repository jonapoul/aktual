package aktual.budget.budgeting.domain

import aktual.budget.BudgetSyncController
import aktual.budget.db.dao.BudgetDao
import aktual.budget.db.dao.DatabaseTables.REFLECT_BUDGETS
import aktual.budget.db.dao.DatabaseTables.ZERO_BUDGETS
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.model.Amount
import aktual.budget.model.BudgetType
import aktual.budget.model.CategoryId
import aktual.budget.model.LocalChange
import aktual.budget.model.MessageValue
import aktual.budget.model.SyncedPrefKey
import aktual.budget.model.messageValue
import aktual.core.Calendar
import aktual.di.BudgetScope
import dev.zacsweers.metro.ContributesBinding
import kotlin.math.floor
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.yearMonth

/**
 * The per-category and month-wide actions of packages/loot-core/src/server/budget/actions.ts. Each
 * call sends its changes in one batch, and the UI picks them up through [BudgetMonthCalculator].
 */
interface BudgetWriter {
  suspend fun setBudget(month: YearMonth, category: CategoryId, amount: Amount)

  // This month and every later month in the budget's bounds
  suspend fun setCarryover(from: YearMonth, category: CategoryId, enabled: Boolean)

  suspend fun copySinglePreviousMonth(month: YearMonth, category: CategoryId)

  suspend fun setSingleAverage(month: YearMonth, category: CategoryId, months: Int)

  // Upstream's UI only offers this for tracking budgets
  suspend fun copyUntilYearEnd(month: YearMonth, category: CategoryId)

  suspend fun copyPreviousMonth(month: YearMonth)

  suspend fun setZero(month: YearMonth)

  suspend fun setAverage(month: YearMonth, months: Int)
}

@ContributesBinding(BudgetScope::class)
class BudgetWriterImpl(
  private val syncController: BudgetSyncController,
  private val budgetDao: BudgetDao,
  private val preferencesDao: PreferencesDao,
  private val calendar: Calendar,
) : BudgetWriter {
  override suspend fun setBudget(month: YearMonth, category: CategoryId, amount: Amount) = send {
    set(month, category, amount)
  }

  // setCategoryCarryover()
  override suspend fun setCarryover(from: YearMonth, category: CategoryId, enabled: Boolean) {
    val end = bounds().endInclusive
    send {
      for (month in from..end) {
        upsert(month, category, column = "carryover", enabled.messageValue())
      }
    }
  }

  // copySinglePreviousMonth()
  override suspend fun copySinglePreviousMonth(month: YearMonth, category: CategoryId) {
    val previous = budgetDao.budgetsInMonth(type(), month.previous())
    val amount = previous.firstOrNull { it.category == category }?.amount ?: Amount.Zero
    send { set(month, category, amount) }
  }

  // setNMonthAvg()
  override suspend fun setSingleAverage(month: YearMonth, category: CategoryId, months: Int) {
    val type = type()
    val isIncome = budgetDao.categories().firstOrNull { it.id == category }?.is_income == true
    val average = average(type, month, category, isIncome, months)
    send(type) { set(month, category, average) }
  }

  // copyUntilYearEnd()
  override suspend fun copyUntilYearEnd(month: YearMonth, category: CategoryId) {
    val type = type()
    val amount =
      budgetDao.budgetsInMonth(type, month).firstOrNull { it.category == category }?.amount
        ?: Amount.Zero
    val end = minOf(YearMonth(month.year, Month.DECEMBER), bounds().endInclusive)
    send(type) {
      for (future in month.next()..end) set(future, category, amount)
    }
  }

  // copyPreviousMonth()
  override suspend fun copyPreviousMonth(month: YearMonth) {
    val type = type()
    val previous = budgetDao.budgetsInMonth(type, month.previous())
    send(type) {
      previous
        .filter { type == Tracking || !it.isIncome }
        .filter { !it.isHidden && !it.isGroupHidden }
        .forEach { set(month, it.category, it.amount) }
    }
  }

  // setZero(). Unlike the others, upstream zeroes hidden categories too
  override suspend fun setZero(month: YearMonth) {
    val type = type()
    val categories = budgetDao.categories()
    send(type) {
      categories
        .filter { type == Tracking || it.is_income != true }
        .forEach { set(month, it.id, Zero) }
    }
  }

  // set3MonthAvg(), set6MonthAvg() and set12MonthAvg()
  override suspend fun setAverage(month: YearMonth, months: Int) {
    val type = type()
    val categories =
      budgetDao
        .categories()
        .filter { !it.hidden && !it.group_hidden }
        .filter { type == Tracking || it.is_income != true }
    val averages = categories.associate {
      it.id to average(type, month, it.id, it.is_income == true, months)
    }
    send(type) { averages.forEach { (category, amount) -> set(month, category, amount) } }
  }

  // getCategoryAverage(). Expense averages are flipped to a positive budget
  private suspend fun average(
    type: BudgetType,
    month: YearMonth,
    category: CategoryId,
    isIncome: Boolean,
    count: Int,
  ): Amount {
    val months = averageMonths(type, month, category, count)
    if (months.isEmpty()) return Zero
    val spent =
      budgetDao
        .spentByMonth(months.last().firstDay, months.first().lastDay)
        .filter { it.category == category }
        .sumOf { it.total }
    // Math.round() rounds halves up
    val average = floor(spent.toDouble() / months.size + HALF).toLong()
    return Amount(if (isIncome) average else -average)
  }

  // getAverageMonths(). Newest first, from the month before but never later than last month
  private suspend fun averageMonths(
    type: BudgetType,
    month: YearMonth,
    category: CategoryId,
    count: Int,
  ): List<YearMonth> {
    val first = minOf(month.previous(), calendar.today().yearMonth.previous())
    val firstActivity = budgetDao.firstActivityMonth(type, category, first)
    return generateSequence(first) { it.previous() }
      .take(count)
      .takeWhile { firstActivity == null || it >= firstActivity }
      .toList()
  }

  private suspend fun bounds(): ClosedRange<YearMonth> =
    budgetBounds(budgetDao.observeEarliestTransactionDate().first(), calendar.today())

  // isTrackingBudget()
  private suspend fun type(): BudgetType =
    BudgetType.from(preferencesDao[SyncedPrefKey.Global.BudgetType]) ?: Envelope

  private suspend fun send(known: BudgetType? = null, block: suspend Batch.() -> Unit) {
    val batch = Batch(known ?: type())
    batch.block()
    if (batch.changes.isNotEmpty()) syncController.syncChanges(batch.changes)
  }

  private inner class Batch(private val type: BudgetType) {
    val changes = mutableListOf<LocalChange>()

    private val table =
      when (type) {
        Envelope -> ZERO_BUDGETS
        Tracking -> REFLECT_BUDGETS
      }

    // setBudget()
    suspend fun set(month: YearMonth, category: CategoryId, amount: Amount) =
      upsert(month, category, column = "amount", MessageValue.Number(amount.toLong()))

    // As upstream, an existing row for the month and category is updated whatever its ID
    suspend fun upsert(
      month: YearMonth,
      category: CategoryId,
      column: String,
      value: MessageValue,
    ) {
      val existing = budgetDao.budgetId(type, month, category)
      if (existing != null) {
        changes += LocalChange(table, existing, column, value)
      } else {
        val id = "${month.dbMonth}-${category.value}"
        changes += LocalChange(table, id, "month", MessageValue.Number(month.dbMonth))
        changes += LocalChange(table, id, "category", category.value.messageValue())
        changes += LocalChange(table, id, column, value)
      }
    }
  }
}

private const val HALF = 0.5
private const val DB_MONTH_FACTOR = 100

private val YearMonth.dbMonth: Long
  get() = year.toLong() * DB_MONTH_FACTOR + month.number

private fun YearMonth.previous(): YearMonth = minus(1, MONTH)

private fun YearMonth.next(): YearMonth = plus(1, MONTH)
