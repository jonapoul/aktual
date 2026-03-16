package aktual.budget.db.converters

import aktual.budget.model.BalanceType
import aktual.budget.model.CustomReportMode
import aktual.budget.model.DateRangeType
import aktual.budget.model.GraphType
import aktual.budget.model.GroupBy
import aktual.budget.model.Interval
import aktual.budget.model.Operator
import aktual.budget.model.RuleStage
import aktual.budget.model.SortBy
import aktual.budget.model.WidgetType
import alakazam.kotlin.SerializableByString
import androidx.room3.TypeConverter
import kotlin.enums.enumEntries

private inline fun <reified E> enumFromString(value: String): E
  where E : Enum<E>, E : SerializableByString = enumEntries<E>().first { it.value == value }

internal class BalanceTypeConverter {
  @TypeConverter fun from(value: String): BalanceType = enumFromString(value)

  @TypeConverter fun to(value: BalanceType): String = value.value
}

internal class CustomReportModeConverter {
  @TypeConverter fun from(value: String): CustomReportMode = enumFromString(value)

  @TypeConverter fun to(value: CustomReportMode): String = value.value
}

internal class DateRangeTypeConverter {
  @TypeConverter fun from(value: String): DateRangeType = enumFromString(value)

  @TypeConverter fun to(value: DateRangeType): String = value.value
}

internal class GraphTypeConverter {
  @TypeConverter fun from(value: String): GraphType = enumFromString(value)

  @TypeConverter fun to(value: GraphType): String = value.value
}

internal class GroupByConverter {
  @TypeConverter fun from(value: String): GroupBy = enumFromString(value)

  @TypeConverter fun to(value: GroupBy): String = value.value
}

internal class IntervalConverter {
  @TypeConverter fun from(value: String): Interval = enumFromString(value)

  @TypeConverter fun to(value: Interval): String = value.value
}

internal class OperatorConverter {
  @TypeConverter fun from(value: String): Operator = enumFromString(value)

  @TypeConverter fun to(value: Operator): String = value.value
}

internal class RuleStageConverter {
  @TypeConverter fun from(value: String): RuleStage = enumFromString(value)

  @TypeConverter fun to(value: RuleStage): String = value.value
}

internal class SortByConverter {
  @TypeConverter fun from(value: String): SortBy = enumFromString(value)

  @TypeConverter fun to(value: SortBy): String = value.value
}

internal class WidgetTypeConverter {
  @TypeConverter fun from(value: String): WidgetType = enumFromString(value)

  @TypeConverter fun to(value: WidgetType): String = value.value
}
