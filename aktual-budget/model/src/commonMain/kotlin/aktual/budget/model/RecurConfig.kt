package aktual.budget.model

import alakazam.kotlin.SerializableByString
import alakazam.kotlin.enumStringSerializer
import kotlinx.datetime.LocalDate
import kotlinx.serialization.KSerializer
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

@Serializable(RecurFrequency.Serializer::class)
enum class RecurFrequency(override val value: String) : SerializableByString {
  Daily("daily"),
  Weekly("weekly"),
  Monthly("monthly"),
  Yearly("yearly");

  object Serializer : KSerializer<RecurFrequency> by enumStringSerializer()
}

@Serializable
data class RecurPattern(
  @SerialName("value") val value: Int,
  @SerialName("type") val type: RecurType,
)

@Serializable(RecurType.Serializer::class)
enum class RecurType(override val value: String) : SerializableByString {
  Sunday("SU"),
  Monday("MO"),
  Tuesday("TU"),
  Wednesday("WE"),
  Thursday("TH"),
  Friday("FR"),
  Saturday("SA"),
  Day("day");

  object Serializer : KSerializer<RecurType> by enumStringSerializer()
}

@Serializable(RecurEndMode.Serializer::class)
enum class RecurEndMode(override val value: String) : SerializableByString {
  Never("never"),
  AfterNOccurrences("after_n_occurrences"),
  OnDate("on_date");

  object Serializer : KSerializer<RecurEndMode> by enumStringSerializer()
}

@Serializable(WeekendSolveMode.Serializer::class)
enum class WeekendSolveMode(override val value: String) : SerializableByString {
  Before("before"),
  After("after");

  object Serializer : KSerializer<WeekendSolveMode> by enumStringSerializer()
}
