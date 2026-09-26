package aktual.budget.model

import fallback.serializer.Fallback
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class DateRangeType {
  @SerialName("This week") ThisWeek,
  @SerialName("Last week") LastWeek,
  @SerialName("This month") ThisMonth,
  @SerialName("Last month") LastMonth,
  @SerialName("Last 3 months") Last3Months,
  @SerialName("Last 6 months") Last6Months,
  @SerialName("Last 12 months") Last12Months,
  @SerialName("Year to date") YearToDate,
  @SerialName("Last year") LastYear,
  @SerialName("All time") AllTime,
  @Fallback Unknown;

  companion object {
    val known: ImmutableList<DateRangeType> = entries.filter { it != Unknown }.toImmutableList()
  }
}
