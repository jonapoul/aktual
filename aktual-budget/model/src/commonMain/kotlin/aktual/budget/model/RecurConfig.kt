package aktual.budget.model

import fallback.serializer.Fallback
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecurConfig(
  @SerialName("frequency") val frequency: RecurFrequency,
  @SerialName("start") val start: LocalDate,
  @SerialName("interval") val interval: Int? = null,
  @SerialName("patterns") val patterns: List<RecurPattern>? = null,
  @SerialName("skipWeekend") val skipWeekend: Boolean? = null,
  @SerialName("endMode") val endMode: RecurEndMode? = null,
  @SerialName("endOccurrences") val endOccurrences: Int? = null,
  @SerialName("endDate") val endDate: LocalDate? = null,
  @SerialName("weekendSolveMode") val weekendSolveMode: WeekendSolveMode? = null,
)

@Serializable
enum class RecurFrequency {
  @SerialName("daily") Daily,
  @SerialName("weekly") Weekly,
  @SerialName("monthly") Monthly,
  @SerialName("yearly") Yearly,
  @Fallback Unknown,
}

@Serializable
data class RecurPattern(
  @SerialName("value") val value: Int,
  @SerialName("type") val type: RecurType,
)

@Serializable
enum class RecurType {
  @SerialName("SU") Sunday,
  @SerialName("MO") Monday,
  @SerialName("TU") Tuesday,
  @SerialName("WE") Wednesday,
  @SerialName("TH") Thursday,
  @SerialName("FR") Friday,
  @SerialName("SA") Saturday,
  @SerialName("day") Day,
  @Fallback Unknown,
}

@Serializable
enum class RecurEndMode {
  @SerialName("never") Never,
  @SerialName("after_n_occurrences") AfterNOccurrences,
  @SerialName("on_date") OnDate,
  @Fallback Unknown,
}

@Serializable
enum class WeekendSolveMode {
  @SerialName("before") Before,
  @SerialName("after") After,
  @Fallback Unknown,
}
