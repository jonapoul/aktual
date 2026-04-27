package aktual.budget.db.dao

/**
 * Table names for all synced tables in the budget database, for use with [SyncDao.sendMessages].
 */
object DatabaseTables {
  const val ACCOUNTS = "accounts"
  const val CATEGORIES = "categories"
  const val CATEGORY_GROUPS = "category_groups"
  const val CATEGORY_MAPPING = "category_mapping"
  const val CUSTOM_REPORTS = "custom_reports"
  const val DASHBOARD = "dashboard"
  const val DASHBOARD_PAGES = "dashboard_pages"
  const val NOTES = "notes"
  const val PAYEES = "payees"
  const val PAYEE_LOCATIONS = "payee_locations"
  const val PAYEE_MAPPING = "payee_mapping"
  const val PREFERENCES = "preferences"
  const val REFLECT_BUDGETS = "reflect_budgets"
  const val RULES = "rules"
  const val SCHEDULES = "schedules"
  const val TAGS = "tags"
  const val TRANSACTIONS = "transactions"
  const val TRANSACTION_FILTERS = "transaction_filters"
  const val ZERO_BUDGETS = "zero_budgets"
}
