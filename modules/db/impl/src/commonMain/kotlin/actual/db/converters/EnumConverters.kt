package actual.db.converters

import actual.budget.model.AccountSyncSource
import actual.budget.model.BalanceType
import actual.budget.model.ConditionOperator
import actual.budget.model.CustomReportMode
import actual.budget.model.DateRangeType
import actual.budget.model.GraphType
import actual.budget.model.GroupBy
import actual.budget.model.SortBy
import androidx.room.TypeConverter

@Suppress("TooManyFunctions")
internal object EnumConverters {
  @TypeConverter
  fun groupByTo(value: GroupBy): String = value.toString()

  @TypeConverter
  fun groupByFrom(value: String): GroupBy = GroupBy.fromString(value)

  @TypeConverter
  fun conditionOperatorTo(value: ConditionOperator): String = value.toString()

  @TypeConverter
  fun conditionOperatorFrom(value: String): ConditionOperator = ConditionOperator.fromString(value)

  @TypeConverter
  fun graphTypeTo(value: GraphType): String = value.toString()

  @TypeConverter
  fun graphTypeFrom(value: String): GraphType = GraphType.fromString(value)

  @TypeConverter
  fun sortByTo(value: SortBy): String = value.toString()

  @TypeConverter
  fun sortByFrom(value: String): SortBy = SortBy.fromString(value)

  @TypeConverter
  fun customReportModeTo(value: CustomReportMode): String = value.toString()

  @TypeConverter
  fun customReportModeFrom(value: String): CustomReportMode = CustomReportMode.fromString(value)

  @TypeConverter
  fun balanceTypeTo(value: BalanceType): String = value.toString()

  @TypeConverter
  fun balanceTypeFrom(value: String): BalanceType = BalanceType.fromString(value)

  @TypeConverter
  fun accountSyncSourceTo(value: AccountSyncSource): String = value.toString()

  @TypeConverter
  fun accountSyncSourceFrom(value: String): AccountSyncSource = AccountSyncSource.fromString(value)

  @TypeConverter
  fun dateRangeTypeTo(value: DateRangeType): String = value.toString()

  @TypeConverter
  fun dateRangeTypeFrom(value: String): DateRangeType = DateRangeType.fromString(value)
}
