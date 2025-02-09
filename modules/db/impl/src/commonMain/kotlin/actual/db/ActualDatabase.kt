package actual.db

import actual.db.converter.ConditionConverter
import actual.db.dao.SpreadsheetCellDao
import actual.db.dao.TransactionDao
import actual.db.entity.AccountEntity
import actual.db.entity.BankEntity
import actual.db.entity.CategoryEntity
import actual.db.entity.CategoryGroupEntity
import actual.db.entity.CategoryMappingEntity
import actual.db.entity.CreatedBudgetEntity
import actual.db.entity.CustomReportEntity
import actual.db.entity.KvCacheEntity
import actual.db.entity.KvCacheKeyEntity
import actual.db.entity.MessageClockEntity
import actual.db.entity.MessageCrdtEntity
import actual.db.entity.MetaEntity
import actual.db.entity.MigrationEntity
import actual.db.entity.NoteEntity
import actual.db.entity.PayeeEntity
import actual.db.entity.PayeeMappingEntity
import actual.db.entity.PendingTransactionEntity
import actual.db.entity.PreferenceEntity
import actual.db.entity.ReflectBudgetEntity
import actual.db.entity.RuleEntity
import actual.db.entity.ScheduleEntity
import actual.db.entity.ScheduleJsonPathEntity
import actual.db.entity.ScheduleNextDateEntity
import actual.db.entity.TransactionEntity
import actual.db.entity.TransactionFilterEntity
import actual.db.entity.ZeroBudgetEntity
import actual.db.entity.ZeroBudgetMonthEntity
import actual.db.migration.AlterAccountsSortOrder
import actual.db.migration.AlterAccountsSyncSource
import actual.db.migration.AlterAccountsType
import actual.db.migration.AlterAccountsType2
import actual.db.migration.AlterCategoriesHidden
import actual.db.migration.AlterCategoryGroups
import actual.db.migration.AlterCustomGroupsIncludeCurrent
import actual.db.migration.AlterCustomReportsSortBy
import actual.db.migration.AlterCustomReportsSortBy2
import actual.db.migration.AlterGoals
import actual.db.migration.AlterLongGoal
import actual.db.migration.AlterPayeesFavorite
import actual.db.migration.AlterPayeesLearnCategories
import actual.db.migration.AlterRulesConditions
import actual.db.migration.AlterSchedulesName
import actual.db.migration.AlterSchedulesNextDateTombstone
import actual.db.migration.AlterTransactionsClearedPending
import actual.db.migration.AlterTransactionsParentId
import actual.db.migration.AlterTransactionsReconciled
import actual.db.migration.CreateCustomReports
import actual.db.migration.CreateDashboard
import actual.db.migration.CreateMessagesCrdtSearchIndex
import actual.db.migration.CreatePayees
import actual.db.migration.CreatePreferences
import actual.db.migration.CreateRules
import actual.db.migration.CreateSchedules
import actual.db.migration.CreateTransactionFilters
import actual.db.migration.CreateTransactionIndices
import actual.db.migration.CreateTransactionViews
import actual.db.migration.CreateTransactionViews2
import actual.db.migration.DropDbVersion
import actual.db.migration.DropPayeeRules
import actual.db.migration.DropViews
import actual.db.migration.FixDashboard
import actual.db.migration.RemoveCache
import actual.db.migration.SetTransactionsSchedule
import actual.db.migration.UpdateCategoryGroup
import actual.db.migration.UpdateCustomReports
import actual.db.migration.UpdateSpreadsheetCells
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import kotlin.coroutines.CoroutineContext

@Database(
  version = ActualDatabase.VERSION,
  exportSchema = true,
  entities = [
    AccountEntity::class,
    BankEntity::class,
    CategoryEntity::class,
    CategoryGroupEntity::class,
    CategoryMappingEntity::class,
    CreatedBudgetEntity::class,
    CustomReportEntity::class,
    KvCacheEntity::class,
    KvCacheKeyEntity::class,
    MessageClockEntity::class,
    MessageCrdtEntity::class,
    MetaEntity::class,
    MigrationEntity::class,
    NoteEntity::class,
    PayeeEntity::class,
    PayeeMappingEntity::class,
    PendingTransactionEntity::class,
    PreferenceEntity::class,
    ReflectBudgetEntity::class,
    RuleEntity::class,
    ScheduleEntity::class,
    ScheduleJsonPathEntity::class,
    ScheduleNextDateEntity::class,
    TransactionEntity::class,
    TransactionFilterEntity::class,
    ZeroBudgetEntity::class,
    ZeroBudgetMonthEntity::class,
  ],
)
@ConstructedBy(ActualDatabaseConstructor::class)
@TypeConverters(
  ConditionConverter::class,
)
abstract class ActualDatabase : RoomDatabase() {
  abstract fun spreadsheetCells(): SpreadsheetCellDao
  abstract fun transactions(): TransactionDao

  companion object {
    // Grab the first 9 digits from the migration timestamp, from the filenames in:
    // https://github.com/actualbudget/actual/tree/master/packages/loot-core/migrations
    const val VERSION: Int = AlterCustomReportsSortBy2.VERSION

    const val FILENAME = "db.sqlite"

    fun migrations(): List<Migration> = listOf(
      DropDbVersion(),
      CreatePayees(),
      UpdateCategoryGroup(),
      CreateTransactionIndices(),
      UpdateSpreadsheetCells(),
      AlterTransactionsClearedPending(),
      CreateRules(),
      AlterTransactionsParentId(),
      CreateTransactionViews(),
      CreateMessagesCrdtSearchIndex(),
      CreateTransactionViews2(),
      DropViews(),
      AlterAccountsSortOrder(),
      CreateSchedules(),
      RemoveCache(),
      AlterRulesConditions(),
      AlterSchedulesName(),
      DropPayeeRules(),
      AlterCategoriesHidden(),
      AlterAccountsType(),
      CreateTransactionFilters(),
      AlterAccountsType2(),
      AlterSchedulesNextDateTombstone(),
      AlterGoals(),
      AlterTransactionsReconciled(),
      AlterAccountsSyncSource(),
      CreateCustomReports(),
      AlterCategoryGroups(),
      AlterCustomGroupsIncludeCurrent(),
      SetTransactionsSchedule(),
      AlterPayeesFavorite(),
      AlterLongGoal(),
      UpdateCustomReports(),
      CreateDashboard(),
      CreatePreferences(),
      FixDashboard(),
      AlterCustomReportsSortBy(),
      AlterPayeesLearnCategories(),
      AlterCustomReportsSortBy2(),
    )

    @Suppress("SpreadOperator")
    fun Builder<ActualDatabase>.build(
      context: CoroutineContext,
    ): ActualDatabase = fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
      .setQueryCoroutineContext(context)
      .addMigrations(*migrations().toTypedArray())
      .build()
  }
}
