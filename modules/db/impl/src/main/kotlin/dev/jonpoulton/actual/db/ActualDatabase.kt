package dev.jonpoulton.actual.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
  entities = [
//     AccountEntity::class,
    BankEntity::class,
    CategoryEntity::class,
    CategoryGroupEntity::class,
    CategoryMappingEntity::class,
    CreatedBudgetEntity::class,
    CustomReportEntity::class,
//     DbVersionEntity::class,
    KvCacheEntity::class,
    KvCacheKeyEntity::class,
    MessagesClockEntity::class,
    MessagesCrdtEntity::class,
    MetaEntity::class,
    MigrationEntity::class,
    NoteEntity::class,
    PayeeEntity::class,
    PayeeMappingEntity::class,
    PendingTransactionEntity::class,
    RuleEntity::class,
    ReflectBudgetEntity::class,
    ScheduleEntity::class,
    ScheduleNextDateEntity::class,
    ScheduleJsonPathEntity::class,
//     SpreadsheetCellEntity::class,
    SqliteStat1Entity::class,
    TransactionEntity::class,
    TransactionFilterEntity::class,
    ZeroBudgetMonthEntity::class,
    ZeroBudgetEntity::class,
  ],
  version = 1,
)
abstract class ActualDatabase : RoomDatabase() {
  abstract fun accounts(): AccountDao
  abstract fun banks(): BankDao
  abstract fun categories(): CategoryDao
  abstract fun categoryGroups(): CategoryGroupDao
  abstract fun categoryMapping(): CategoryMappingDao
  abstract fun createdBudgets(): CreatedBudgetDao
  abstract fun dbVersions(): DbVersionDao
  abstract fun messagesClock(): MessagesClockDao
  abstract fun messagesCrdt(): MessagesCrdtDao
  abstract fun migrations(): MigrationDao
  abstract fun pendingTransactions(): PendingTransactionDao
  abstract fun spreadsheetCells(): SpreadsheetCellDao
  abstract fun transactions(): TransactionDao
}
