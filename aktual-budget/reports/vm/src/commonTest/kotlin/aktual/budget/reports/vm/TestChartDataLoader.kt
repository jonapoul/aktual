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
import aktual.core.Calendar
import aktual.prefs.CurrencyPreferences
import aktual.prefs.FormatPreferences
import aktual.prefs.Preference
import alakazam.kotlin.CoroutineContexts
import kotlinx.coroutines.flow.flowOf

internal fun BudgetDatabase.chartDataLoader(
  contexts: CoroutineContexts,
  calendar: Calendar,
  formatPreferences: FormatPreferences = TestFormatPreferences,
  currencyPreferences: CurrencyPreferences = TestCurrencyPreferences,
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
    currencyPreferences = currencyPreferences,
    calendar = calendar,
    contexts = contexts,
  )

internal object TestFormatPreferences : FormatPreferences {
  override val hideFraction = fixed(false)
  override val dateFormat = fixed(DateFormat.Default)
  override val firstDayOfWeek = fixed(FirstDayOfWeek.Default)
  override val numberFormat = fixed(NumberFormat.Default)
}

internal object TestCurrencyPreferences : CurrencyPreferences {
  override val currency = fixed(Currency.UsDollar)
  override val symbolPosition = fixed(CurrencySymbolPosition.BeforeAmount)
  override val spaceBetweenAmountAndSymbol = fixed(false)
}

internal fun <T : Any> fixed(fixed: T) =
  object : Preference<T> {
    override val default = fixed

    override suspend fun get() = fixed

    override suspend fun set(value: T?) = Unit

    override fun asFlow() = flowOf(fixed)
  }
