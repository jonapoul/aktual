package aktual.budget.reports.vm

import aktual.budget.budgeting.domain.BudgetMonthCalculatorImpl
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.BudgetDao
import aktual.budget.db.dao.CustomReportsDao
import aktual.budget.db.dao.FormulaDao
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.db.dao.ReportsDao
import aktual.budget.model.Currency
import aktual.budget.model.CurrencySymbolPosition
import aktual.budget.model.DateFormat
import aktual.budget.model.FirstDayOfWeek
import aktual.budget.model.NumberFormat
import aktual.budget.rules.domain.TransactionFilterCompilerImpl
import aktual.core.utils.Calendar
import aktual.prefs.BudgetFormatPreferences
import aktual.prefs.Preference
import alakazam.kotlin.CoroutineContexts
import kotlinx.coroutines.flow.flowOf

internal fun BudgetDatabase.chartDataLoader(
  contexts: CoroutineContexts,
  calendar: Calendar,
  formatPreferences: BudgetFormatPreferences = TestFormatPreferences,
) =
  ChartDataLoader(
    dao = ReportsDao(this, contexts),
    customReports = CustomReportsDao(this, contexts),
    preferences = PreferencesDao(this, contexts),
    formulaDao = FormulaDao(this),
    budgets =
      BudgetMonthCalculatorImpl(
        budgetDao = BudgetDao(this, contexts),
        preferencesDao = PreferencesDao(this, contexts),
        calendar = calendar,
        contexts = contexts,
      ),
    formatPreferences = formatPreferences,
    calendar = calendar,
    contexts = contexts,
    compiler = TransactionFilterCompilerImpl(),
  )

internal object TestFormatPreferences : BudgetFormatPreferences {
  override val numberFormat = fixed(NumberFormat.Default)
  override val hideFraction = fixed(false)
  override val currency = fixed(Currency.UsDollar)
  override val symbolPosition = fixed(CurrencySymbolPosition.BeforeAmount)
  override val spaceBetweenAmountAndSymbol = fixed(false)
  override val dateFormat = fixed(DateFormat.YyyyMmDd)
  override val firstDayOfWeek = fixed(FirstDayOfWeek.Monday)

  override suspend fun changeCurrency(currency: Currency) = Unit
}

internal fun <T : Any> fixed(fixed: T) =
  object : Preference<T> {
    override val default = fixed

    override suspend fun get() = fixed

    override suspend fun set(value: T?) = Unit

    override fun asFlow() = flowOf(fixed)
  }
