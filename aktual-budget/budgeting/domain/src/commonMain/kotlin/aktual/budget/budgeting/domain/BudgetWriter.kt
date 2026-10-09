package aktual.budget.budgeting.domain

import aktual.budget.BudgetSyncController
import aktual.budget.db.dao.BudgetDao
import aktual.budget.db.dao.DatabaseTables.NOTES
import aktual.budget.db.dao.DatabaseTables.REFLECT_BUDGETS
import aktual.budget.db.dao.DatabaseTables.ZERO_BUDGETS
import aktual.budget.db.dao.DatabaseTables.ZERO_BUDGET_MONTHS
import aktual.budget.db.dao.NotesDao
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.model.Amount
import aktual.budget.model.BudgetType
import aktual.budget.model.CategoryId
import aktual.budget.model.CurrencyConfig
import aktual.budget.model.LocalChange
import aktual.budget.model.MessageValue
import aktual.budget.model.NumberFormatConfig
import aktual.budget.model.SyncedPrefKey
import aktual.budget.model.messageValue
import aktual.core.Calendar
import aktual.di.BudgetScope
import aktual.prefs.CurrencyPreferences
import aktual.prefs.FormatPreferences
import dev.zacsweers.metro.ContributesBinding
import kotlin.math.floor
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.format.char
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.yearMonth

/**
 * The per-category and month-wide actions of packages/loot-core/src/server/budget/actions.ts. Each
 * call sends its changes in one batch, and the UI picks them up through [BudgetMonthCalculator].
 */
@Suppress("ComplexInterface")
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

  // Envelope budgets only from here on. A null category is To Budget

  suspend fun transferCategory(month: YearMonth, amount: Amount, from: CategoryId, to: CategoryId?)

  suspend fun transferAvailable(month: YearMonth, amount: Amount, category: CategoryId)

  // A null amount covers all of the overspending
  suspend fun coverOverspending(
    month: YearMonth,
    to: CategoryId,
    from: CategoryId?,
    amount: Amount? = null,
  )

  // A null amount covers all of the overbudgeting
  suspend fun coverOverbudgeted(month: YearMonth, category: CategoryId, amount: Amount? = null)

  // False if there was nothing to hold
  suspend fun holdForNextMonth(month: YearMonth, amount: Amount): Boolean

  suspend fun resetHold(month: YearMonth)
}

@ContributesBinding(BudgetScope::class)
class BudgetWriterImpl(
  private val syncController: BudgetSyncController,
  private val budgetDao: BudgetDao,
  private val preferencesDao: PreferencesDao,
  private val calendar: Calendar,
  private val calculator: BudgetMonthCalculator,
  private val notesDao: NotesDao,
  private val formatPreferences: FormatPreferences,
  private val currencyPreferences: CurrencyPreferences,
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
    val type = type()
    val previous = budgetDao.budgetsInMonth(type, month.previous())
    val amount = previous.firstOrNull { it.category == category }?.amount ?: Amount.Zero
    send(type) { set(month, category, amount) }
  }

  // setNMonthAvg()
  override suspend fun setSingleAverage(month: YearMonth, category: CategoryId, months: Int) {
    val type = type()
    val isIncome = budgetDao.categories().firstOrNull { it.id == category }?.is_income == true
    val window = averageWindow(month, months)
    val average = average(type, window, spentIn(window), category, isIncome)
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
    val window = averageWindow(month, months)
    val spent = spentIn(window)
    val averages = categories.associate {
      it.id to average(type, window, spent, it.id, it.is_income == true)
    }
    send(type) { averages.forEach { (category, amount) -> set(month, category, amount) } }
  }

  // transferCategory()
  override suspend fun transferCategory(
    month: YearMonth,
    amount: Amount,
    from: CategoryId,
    to: CategoryId?,
  ) {
    val sheet = envelope(month) ?: return
    send(Envelope) {
      set(month, from, sheet.budgeted(from) - amount)
      if (to != null) set(month, to, sheet.budgeted(to) + amount)
      movementNote(sheet, amount, sheet.name(from), to?.let(sheet::name) ?: TO_BUDGET)
    }
  }

  // transferAvailable()
  override suspend fun transferAvailable(month: YearMonth, amount: Amount, category: CategoryId) {
    val sheet = envelope(month) ?: return
    val clamped = maxOf(minOf(amount, sheet.toBudget), Amount.Zero)
    send(Envelope) { set(month, category, sheet.budgeted(category) + clamped) }
  }

  // coverOverspending()
  override suspend fun coverOverspending(
    month: YearMonth,
    to: CategoryId,
    from: CategoryId?,
    amount: Amount?,
  ) {
    val sheet = envelope(month) ?: return
    val available = if (from == null) sheet.toBudget else sheet.balance(from)
    val toCover = amount.orDefault(-sheet.balance(to))
    if (toCover <= Zero || available <= Zero) return
    val cover = minOf(toCover, available)
    send(Envelope) {
      if (from != null) set(month, from, sheet.budgeted(from) - cover)
      set(month, to, sheet.budgeted(to) + cover)
      movementNote(sheet, cover, from?.let(sheet::name) ?: TO_BUDGET, sheet.name(to))
    }
  }

  // coverOverbudgeted()
  override suspend fun coverOverbudgeted(month: YearMonth, category: CategoryId, amount: Amount?) {
    val sheet = envelope(month) ?: return
    val available = sheet.balance(category)
    val toCover = amount.orDefault(-sheet.toBudget)
    if (toCover <= Zero || available <= Zero) return
    val cover = minOf(toCover, available)
    send(Envelope) {
      set(month, category, sheet.budgeted(category) - cover)
      movementNote(sheet, cover, sheet.name(category), OVERBUDGETED)
    }
  }

  // holdForNextMonth() and calcBufferedAmount()
  override suspend fun holdForNextMonth(month: YearMonth, amount: Amount): Boolean {
    val sheet = envelope(month) ?: return false
    if (sheet.toBudget <= Zero) return false
    // The stored amount, which isn't the sheet's when an income category is held instead
    val buffered =
      budgetDao
        .observeEnvelopeMonths()
        .first()
        .firstOrNull { it.id.value == month.toString() }
        ?.buffered ?: Amount.Zero
    setBuffer(month, buffered + minOf(maxOf(amount, -buffered), sheet.toBudget))
    return true
  }

  // resetHold()
  override suspend fun resetHold(month: YearMonth) = setBuffer(month, Zero)

  // setBuffer()
  private suspend fun setBuffer(month: YearMonth, amount: Amount) =
    syncController.syncChanges(
      listOf(
        LocalChange(
          dataset = ZERO_BUDGET_MONTHS,
          row = month.toString(),
          column = "buffered",
          value = MessageValue.Number(amount.toLong()),
        ),
      ),
    )

  private suspend fun envelope(month: YearMonth): BudgetMonth.Envelope? =
    calculator.observe(month).first() as? BudgetMonth.Envelope

  // addMovementNotes(). Not translated, since the note is synced
  private suspend fun Batch.movementNote(
    sheet: BudgetMonth.Envelope,
    amount: Amount,
    from: String,
    to: String,
  ) {
    val id = "budget-${sheet.month}"
    val existing = notesDao.getNote(id).orEmpty()
    val displayAmount =
      amount.toString(
        numberFormatConfig =
          NumberFormatConfig(
            format = formatPreferences.numberFormat.get(),
            hideFraction = formatPreferences.hideFraction.get(),
          ),
        currencyConfig =
          CurrencyConfig(
            currency = currencyPreferences.currency.get(),
            position = BeforeAmount,
            includeSpace = false,
          ),
        includeSign = false,
        isPrivacyEnabled = false,
        includeSymbol = false,
      )
    val day = NOTE_DAY_FORMAT.format(calendar.today())
    val note = "- Reassigned $displayAmount from $from → $to on $day"
    val text = if (existing.isEmpty()) note else "$existing\n$note"
    changes += LocalChange(NOTES, id, "note", text.messageValue())
  }

  // getCategoryAverage(). Expense averages are flipped to a positive budget
  private suspend fun average(
    type: BudgetType,
    window: List<YearMonth>,
    spent: Map<MonthCategory, Long>,
    category: CategoryId,
    isIncome: Boolean,
  ): Amount {
    if (window.isEmpty()) return Zero
    // getAverageMonths() stops before the category's first activity
    val firstActivity = budgetDao.firstActivityMonth(type, category, window.first())
    val months = window.takeWhile { firstActivity == null || it >= firstActivity }
    if (months.isEmpty()) return Zero
    val sum = months.sumOf { spent[MonthCategory(it, category)] ?: 0L }
    // Math.round() rounds halves up
    val average = floor(sum.toDouble() / months.size + HALF).toLong()
    return Amount(if (isIncome) average else -average)
  }

  // getAverageMonths(). Newest first, from the month before but never later than last month
  private fun averageWindow(month: YearMonth, count: Int): List<YearMonth> {
    val first = minOf(month.previous(), calendar.today().yearMonth.previous())
    return generateSequence(first) { it.previous() }.take(count).toList()
  }

  // Every category's spending across the window, fetched once for all of them
  private suspend fun spentIn(window: List<YearMonth>): Map<MonthCategory, Long> =
    if (window.isEmpty()) {
      emptyMap()
    } else {
      budgetDao.spentByMonth(window.last().firstDay, window.first().lastDay).associate {
        MonthCategory(it.month.toYearMonth(), it.category) to it.total
      }
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

private const val TO_BUDGET = "To Budget"
private const val OVERBUDGETED = "Overbudgeted"

// MMMM dd
private val NOTE_DAY_FORMAT = LocalDate.Format {
  monthName(ENGLISH_FULL)
  char(' ')
  day()
}

// Upstream treats a zero amount as unset
private fun Amount?.orDefault(default: Amount): Amount = this?.takeIf { it != Zero } ?: default

// Upstream's budget-* and leftover-* cells, which are zero for unknown categories
private fun BudgetMonth.Envelope.budgeted(id: CategoryId): Amount =
  categories.firstOrNull { it.id == id }?.budgeted ?: Amount.Zero

private fun BudgetMonth.Envelope.balance(id: CategoryId): Amount =
  categories.firstOrNull { it.id == id }?.balance ?: Amount.Zero

private fun BudgetMonth.Envelope.name(id: CategoryId): String =
  categories.firstOrNull { it.id == id }?.name.orEmpty()

private const val HALF = 0.5
private const val DB_MONTH_FACTOR = 100

private val YearMonth.dbMonth: Long
  get() = year.toLong() * DB_MONTH_FACTOR + month.number

private fun Long.toYearMonth(): YearMonth =
  YearMonth(year = (this / DB_MONTH_FACTOR).toInt(), month = (this % DB_MONTH_FACTOR).toInt())

private fun YearMonth.previous(): YearMonth = minus(1, MONTH)

private fun YearMonth.next(): YearMonth = plus(1, MONTH)
