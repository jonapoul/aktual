package aktual.budget.db

import aktual.budget.db.converters.AccountIdConverter
import aktual.budget.db.converters.AccountSyncSourceConverter
import aktual.budget.db.converters.AmountConverter
import aktual.budget.db.converters.BalanceTypeConverter
import aktual.budget.db.converters.BankIdConverter
import aktual.budget.db.converters.CategoryGroupIdConverter
import aktual.budget.db.converters.CategoryIdConverter
import aktual.budget.db.converters.CustomReportIdConverter
import aktual.budget.db.converters.CustomReportModeConverter
import aktual.budget.db.converters.DateRangeTypeConverter
import aktual.budget.db.converters.GraphTypeConverter
import aktual.budget.db.converters.GroupByConverter
import aktual.budget.db.converters.IntervalConverter
import aktual.budget.db.converters.JsonArrayConverter
import aktual.budget.db.converters.JsonElementConverter
import aktual.budget.db.converters.JsonObjectConverter
import aktual.budget.db.converters.LocalDateConverter
import aktual.budget.db.converters.OperatorConverter
import aktual.budget.db.converters.PayeeIdConverter
import aktual.budget.db.converters.ReportConditionListConverter
import aktual.budget.db.converters.ReportDateConverter
import aktual.budget.db.converters.RuleIdConverter
import aktual.budget.db.converters.RuleStageConverter
import aktual.budget.db.converters.ScheduleIdConverter
import aktual.budget.db.converters.ScheduleJsonPathIndexConverter
import aktual.budget.db.converters.ScheduleNextDateIdConverter
import aktual.budget.db.converters.SelectedCategoryListConverter
import aktual.budget.db.converters.SortByConverter
import aktual.budget.db.converters.SyncedPrefKeyConverter
import aktual.budget.db.converters.TimestampConverter
import aktual.budget.db.converters.TransactionFilterIdConverter
import aktual.budget.db.converters.TransactionIdConverter
import aktual.budget.db.converters.UuidConverter
import aktual.budget.db.converters.WidgetIdConverter
import aktual.budget.db.converters.WidgetTypeConverter
import aktual.budget.db.converters.ZeroBudgetMonthIdConverter
import aktual.budget.db.dao.AccountsDao
import aktual.budget.db.dao.BanksDao
import aktual.budget.db.dao.CategoriesDao
import aktual.budget.db.dao.CategoryGroupsDao
import aktual.budget.db.dao.CategoryMappingDao
import aktual.budget.db.dao.CustomReportsDao
import aktual.budget.db.dao.DashboardDao
import aktual.budget.db.dao.KvCacheDao
import aktual.budget.db.dao.KvCacheKeyDao
import aktual.budget.db.dao.MessagesClockDao
import aktual.budget.db.dao.MessagesCrdtDao
import aktual.budget.db.dao.MetaDao
import aktual.budget.db.dao.MigrationsDao
import aktual.budget.db.dao.NotesDao
import aktual.budget.db.dao.PayeeMappingDao
import aktual.budget.db.dao.PayeesDao
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.db.dao.ReflectBudgetsDao
import aktual.budget.db.dao.RulesDao
import aktual.budget.db.dao.ScheduleJsonPathsDao
import aktual.budget.db.dao.SchedulesDao
import aktual.budget.db.dao.SchedulesNextDateDao
import aktual.budget.db.dao.TransactionFiltersDao
import aktual.budget.db.dao.TransactionsDao
import aktual.budget.db.dao.ZeroBudgetMonthsDao
import aktual.budget.db.dao.ZeroBudgetsDao
import aktual.budget.db.model.Account
import aktual.budget.db.model.Bank
import aktual.budget.db.model.Category
import aktual.budget.db.model.CategoryGroup
import aktual.budget.db.model.CategoryMapping
import aktual.budget.db.model.CreatedBudget
import aktual.budget.db.model.CustomReport
import aktual.budget.db.model.Dashboard
import aktual.budget.db.model.KvCache
import aktual.budget.db.model.KvCacheKey
import aktual.budget.db.model.MessagesClock
import aktual.budget.db.model.MessagesCrdt
import aktual.budget.db.model.Meta
import aktual.budget.db.model.Migration
import aktual.budget.db.model.Note
import aktual.budget.db.model.Payee
import aktual.budget.db.model.PayeeMapping
import aktual.budget.db.model.PendingTransaction
import aktual.budget.db.model.Preference
import aktual.budget.db.model.ReflectBudget
import aktual.budget.db.model.Rule
import aktual.budget.db.model.Schedule
import aktual.budget.db.model.ScheduleJsonPath
import aktual.budget.db.model.ScheduleNextDate
import aktual.budget.db.model.Transaction
import aktual.budget.db.model.TransactionFilter
import aktual.budget.db.model.VCategories
import aktual.budget.db.model.VPayees
import aktual.budget.db.model.VSchedules
import aktual.budget.db.model.VTransactions
import aktual.budget.db.model.VTransactionsInternal
import aktual.budget.db.model.VTransactionsInternalAlive
import aktual.budget.db.model.ZeroBudget
import aktual.budget.db.model.ZeroBudgetMonth
import aktual.budget.model.BudgetId
import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.TypeConverters

@Database(
  entities =
    [
      Account::class,
      Bank::class,
      Category::class,
      CategoryGroup::class,
      CategoryMapping::class,
      CreatedBudget::class,
      CustomReport::class,
      Dashboard::class,
      KvCache::class,
      KvCacheKey::class,
      MessagesClock::class,
      MessagesCrdt::class,
      Meta::class,
      Migration::class,
      Note::class,
      Payee::class,
      PayeeMapping::class,
      PendingTransaction::class,
      Preference::class,
      ReflectBudget::class,
      Rule::class,
      Schedule::class,
      ScheduleJsonPath::class,
      ScheduleNextDate::class,
      Transaction::class,
      TransactionFilter::class,
      ZeroBudget::class,
      ZeroBudgetMonth::class,
    ],
  views =
    [
      VCategories::class,
      VPayees::class,
      VTransactionsInternal::class,
      VTransactionsInternalAlive::class,
      VTransactions::class,
      VSchedules::class,
    ],
  version = 1,
  exportSchema = true,
)
@ConstructedBy(BudgetDatabaseConstructor::class)
@TypeConverters(
  AccountIdConverter::class,
  AccountSyncSourceConverter::class,
  AmountConverter::class,
  BalanceTypeConverter::class,
  BankIdConverter::class,
  CategoryGroupIdConverter::class,
  CategoryIdConverter::class,
  CustomReportIdConverter::class,
  CustomReportModeConverter::class,
  DateRangeTypeConverter::class,
  GraphTypeConverter::class,
  GroupByConverter::class,
  IntervalConverter::class,
  JsonArrayConverter::class,
  JsonElementConverter::class,
  JsonObjectConverter::class,
  LocalDateConverter::class,
  OperatorConverter::class,
  PayeeIdConverter::class,
  ReportConditionListConverter::class,
  ReportDateConverter::class,
  RuleIdConverter::class,
  RuleStageConverter::class,
  ScheduleIdConverter::class,
  ScheduleJsonPathIndexConverter::class,
  ScheduleNextDateIdConverter::class,
  SelectedCategoryListConverter::class,
  SortByConverter::class,
  SyncedPrefKeyConverter::class,
  TimestampConverter::class,
  TransactionFilterIdConverter::class,
  TransactionIdConverter::class,
  UuidConverter::class,
  WidgetIdConverter::class,
  WidgetTypeConverter::class,
  ZeroBudgetMonthIdConverter::class,
)
abstract class BudgetDatabase : RoomDatabase() {
  abstract fun accounts(): AccountsDao

  abstract fun banks(): BanksDao

  abstract fun categories(): CategoriesDao

  abstract fun categoryGroups(): CategoryGroupsDao

  abstract fun categoryMapping(): CategoryMappingDao

  abstract fun customReports(): CustomReportsDao

  abstract fun dashboard(): DashboardDao

  abstract fun kvCache(): KvCacheDao

  abstract fun kvCacheKey(): KvCacheKeyDao

  abstract fun messagesClock(): MessagesClockDao

  abstract fun messagesCrdt(): MessagesCrdtDao

  abstract fun meta(): MetaDao

  abstract fun migrations(): MigrationsDao

  abstract fun notes(): NotesDao

  abstract fun payees(): PayeesDao

  abstract fun payeeMapping(): PayeeMappingDao

  abstract fun preferences(): PreferencesDao

  abstract fun reflectBudgets(): ReflectBudgetsDao

  abstract fun rules(): RulesDao

  abstract fun schedules(): SchedulesDao

  abstract fun scheduleJsonPaths(): ScheduleJsonPathsDao

  abstract fun schedulesNextDate(): SchedulesNextDateDao

  abstract fun transactions(): TransactionsDao

  abstract fun transactionFilters(): TransactionFiltersDao

  abstract fun zeroBudgets(): ZeroBudgetsDao

  abstract fun zeroBudgetMonths(): ZeroBudgetMonthsDao

  fun interface BuilderProvider {
    operator fun invoke(id: BudgetId): Builder<BudgetDatabase>
  }
}
