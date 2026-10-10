package aktual.budget.home.vm

import aktual.budget.budgeting.domain.BudgetMonthCalculatorImpl
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.buildDatabase
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.BudgetDao
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.db.dao.ScheduleDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.model.BudgetId
import aktual.budget.model.DbMetadata
import aktual.budget.schedules.domain.SchedulesLoader
import aktual.test.TestBudgetLocalPreferences
import aktual.test.TestCalendar
import aktual.test.inMemoryDriverFactory
import alakazam.test.TestCoroutineContexts
import app.cash.turbine.ReceiveTurbine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.setMain

internal fun BudgetDatabase.createHomeViewModel(
  scope: TestScope,
  calendar: TestCalendar,
  prefs: TestBudgetLocalPreferences = TestBudgetLocalPreferences(DbMetadata()),
  accountsCard: BudgetDatabase = this,
): HomeViewModel {
  Dispatchers.setMain(StandardTestDispatcher(scope.testScheduler))
  val contexts = TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler))
  val calculator =
    BudgetMonthCalculatorImpl(
      budgetDao = BudgetDao(this, contexts),
      preferencesDao = preferences(scope),
      calendar = calendar,
      contexts = contexts,
    )
  val schedulesLoader =
    SchedulesLoader(
      scheduleDao = ScheduleDao(this),
      accountDao = AccountDao(this),
      payeeDao = PayeeDao(this),
      preferencesDao = preferences(scope),
      calendar = calendar,
    )
  val thisMonthLoader = ThisMonthLoader(calculator, calendar)
  val accountsSummaryLoader = AccountsSummaryLoader(AccountDao(this))
  return HomeViewModel(
    localPreferences = prefs,
    thisMonthLoader = thisMonthLoader,
    needsAttentionLoader =
      NeedsAttentionLoader(
        accountsSummaryLoader = accountsSummaryLoader,
        transactionDao = TransactionDao(this),
        thisMonthLoader = thisMonthLoader,
        schedulesLoader = schedulesLoader,
      ),
    accountsSummaryLoader = AccountsSummaryLoader(AccountDao(accountsCard)),
    upcomingSchedulesLoader = UpcomingSchedulesLoader(schedulesLoader, calendar),
  )
}

// Every query on this throws
internal fun closedDatabase(): BudgetDatabase {
  val driver = inMemoryDriverFactory().create(BudgetId("closed"))
  driver.close()
  return buildDatabase(driver)
}

internal fun BudgetDatabase.preferences(scope: TestScope) =
  PreferencesDao(this, TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler)))

// Waits for every card, so a late first load of one isn't mistaken for a change to the other
internal suspend fun ReceiveTurbine<HomeState>.awaitSettled(): HomeState {
  var state = awaitItem()
  while (!state.isSettled) state = awaitItem()
  return state
}

private val HomeState.isSettled: Boolean
  get() = thisMonth != Loading && attention != Loading && accounts != Loading && upcoming != Loading
