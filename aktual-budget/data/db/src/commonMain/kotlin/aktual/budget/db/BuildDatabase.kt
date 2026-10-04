package aktual.budget.db

import app.cash.sqldelight.db.SqlDriver

fun buildDatabase(driver: SqlDriver): BudgetDatabase =
  BudgetDatabase(
    driver = driver,
    account_groupsAdapter = AccountGroupsAdapter,
    accountsAdapter = AccountsAdapter,
    banksAdapter = BanksAdapter,
    categoriesAdapter = CategoriesAdapter,
    category_groupsAdapter = CategoryGroupsAdapter,
    category_mappingAdapter = CategoryMappingAdapter,
    cleanup_groupsAdapter = CleanupGroupsAdapter,
    custom_reportsAdapter = CustomReportsAdapter,
    dashboardAdapter = DashboardAdapter,
    dashboard_pagesAdapter = DashboardPagesAdapter,
    messages_clockAdapter = MessagesClockAdapter,
    messages_crdtAdapter = MessagesCrdtAdapter,
    messages_pendingAdapter = MessagesPendingAdapter,
    payee_mappingAdapter = PayeeMappingAdapter,
    payeesAdapter = PayeesAdapter,
    preferencesAdapter = PreferencesAdapter,
    reflect_budgetsAdapter = ReflectBudgetsAdapter,
    rulesAdapter = RulesAdapter,
    tagsAdapter = TagsAdapter,
    schedulesAdapter = SchedulesAdapter,
    schedules_json_pathsAdapter = SchedulesJsonPathsAdapter,
    schedules_next_dateAdapter = SchedulesNextDateAdapter,
    transactionsAdapter = TransactionsAdapter,
  )
