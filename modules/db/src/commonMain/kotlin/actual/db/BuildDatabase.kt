package actual.db

import app.cash.sqldelight.db.SqlDriver

fun buildDatabase(factory: SqlDriverFactory): BudgetDatabase = buildDatabase(factory.create())

fun buildDatabase(driver: SqlDriver): BudgetDatabase = BudgetDatabase(
  driver = driver,
  accountsAdapter = AccountsAdapter,
  banksAdapter = BanksAdapter,
  categoriesAdapter = CategoriesAdapter,
  category_groupsAdapter = CategoryGroupsAdapter,
  category_mappingAdapter = CategoryMappingAdapter,
  custom_reportsAdapter = CustomReportsAdapter,
  dashboardAdapter = DashboardAdapter,
  messages_clockAdapter = MessagesClockAdapter,
  messages_crdtAdapter = MessagesCrdtAdapter,
  payee_mappingAdapter = PayeeMappingAdapter,
  payeesAdapter = PayeesAdapter,
  preferencesAdapter = PreferencesAdapter,
  reflect_budgetsAdapter = ReflectBudgetsAdapter,
  rulesAdapter = RulesAdapter,
  schedulesAdapter = SchedulesAdapter,
  schedules_json_pathsAdapter = SchedulesJsonPathsAdapter,
  schedules_next_dateAdapter = SchedulesNextDateAdapter,
  transaction_filtersAdapter = TransactionFiltersAdapter,
  transactionsAdapter = TransactionsAdapter,
  zero_budget_monthsAdapter = ZeroBudgetMonthsAdapter,
  zero_budgetsAdapter = ZeroBudgetsAdapter,
)
