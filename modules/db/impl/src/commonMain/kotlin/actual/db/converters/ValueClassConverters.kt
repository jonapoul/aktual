package actual.db.converters

import actual.budget.model.AccountId
import actual.budget.model.BankId
import actual.budget.model.CategoryGroupId
import actual.budget.model.CategoryId
import actual.budget.model.PayeeId
import actual.budget.model.RuleId
import actual.budget.model.ScheduleId
import actual.budget.model.ScheduleJsonPathIndex
import actual.budget.model.ScheduleNextDateId
import actual.budget.model.TransactionFilterId
import actual.budget.model.TransactionId
import actual.budget.model.YearAndMonth
import actual.budget.model.ZeroBudgetMonthId
import androidx.room.TypeConverter

@Suppress("TooManyFunctions")
internal object ValueClassConverters {
  @TypeConverter
  fun accountTo(value: AccountId): String = value.toString()

  @TypeConverter
  fun accountFrom(value: String): AccountId = AccountId(value)

  @TypeConverter
  fun bankTo(value: BankId): String = value.toString()

  @TypeConverter
  fun bankFrom(value: String): BankId = BankId(value)

  @TypeConverter
  fun categoryTo(value: CategoryId): String = value.toString()

  @TypeConverter
  fun categoryFrom(value: String): CategoryId = CategoryId(value)

  @TypeConverter
  fun categoryGroupTo(value: CategoryGroupId): String = value.toString()

  @TypeConverter
  fun categoryGroupFrom(value: String): CategoryGroupId = CategoryGroupId(value)

  @TypeConverter
  fun payeeTo(value: PayeeId): String = value.toString()

  @TypeConverter
  fun payeeFrom(value: String): PayeeId = PayeeId(value)

  @TypeConverter
  fun ruleTo(value: RuleId): String = value.toString()

  @TypeConverter
  fun ruleFrom(value: String): RuleId = RuleId(value)

  @TypeConverter
  fun scheduleTo(value: ScheduleId): String = value.toString()

  @TypeConverter
  fun scheduleFrom(value: String): ScheduleId = ScheduleId(value)

  @TypeConverter
  fun scheduleNextDateTo(value: ScheduleNextDateId): String = value.toString()

  @TypeConverter
  fun scheduleNextDateFrom(value: String): ScheduleNextDateId = ScheduleNextDateId(value)

  @TypeConverter
  fun scheduleJsonPathIndexTo(value: ScheduleJsonPathIndex): String = value.toString()

  @TypeConverter
  fun scheduleJsonPathIndexFrom(value: String): ScheduleJsonPathIndex = ScheduleJsonPathIndex(value)

  @TypeConverter
  fun transactionTo(value: TransactionId): String = value.toString()

  @TypeConverter
  fun transactionFrom(value: String): TransactionId = TransactionId(value)

  @TypeConverter
  fun transactionFilterTo(value: TransactionFilterId): String = value.toString()

  @TypeConverter
  fun transactionFilterFrom(value: String): TransactionFilterId = TransactionFilterId(value)

  @TypeConverter
  fun zeroBudgetMonthTo(value: ZeroBudgetMonthId): String = value.toString()

  @TypeConverter
  fun zeroBudgetMonthFrom(value: String): ZeroBudgetMonthId = ZeroBudgetMonthId(value)

  @TypeConverter
  fun yearAndMonthTo(value: YearAndMonth): String = value.toString()

  @TypeConverter
  fun yearAndMonthFrom(value: String): YearAndMonth = YearAndMonth(value)
}
