@file:Suppress("StringLiteralDuplication")

package aktual.budget.model

import kotlinx.datetime.DateTimeUnit.Companion.DAY
import kotlinx.datetime.DateTimeUnit.Companion.MONTH
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.until
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

enum class UpcomingLengthOptions(val value: String) {
  OneDay("1"),
  OneWeek("7"),
  TwoWeeks("14"),
  OneMonth("oneMonth"),
  CurrentMonth("currentMonth"),
  Custom("custom");

  companion object {
    fun from(value: String): UpcomingLengthOptions? = entries.firstOrNull { it.value == value }
  }
}

// packages/loot-core/src/shared/schedules.ts
@Serializable(UpcomingLengthSerializer::class)
sealed interface UpcomingLength {
  data object OneMonth : UpcomingLength

  data object CurrentMonth : UpcomingLength

  data class Days(val count: Int) : UpcomingLength

  data class Weeks(val count: Int) : UpcomingLength

  data class Months(val count: Int) : UpcomingLength

  data class Years(val count: Int) : UpcomingLength

  fun encode(): String =
    when (this) {
      OneMonth -> "oneMonth"
      CurrentMonth -> "currentMonth"
      is Days -> "$count-day"
      is Months -> "$count-month"
      is Weeks -> "$count-week"
      is Years -> "$count-year"
    }

  companion object {
    fun decode(string: String): UpcomingLength =
      when {
        string == "oneMonth" -> OneMonth
        string == "currentMonth" -> CurrentMonth
        "-" in string -> parseCounted(string)
        else -> Days(count = string.toInt())
      }

    private fun parseCounted(string: String): UpcomingLength {
      val (num, unit) = string.split("-")
      val count = num.toInt()
      return when (unit) {
        "day" -> Days(count)
        "week" -> Weeks(count)
        "month" -> Months(count)
        "year" -> Years(count)
        else -> Days(count = 7)
      }
    }
  }
}

// packages/loot-core/src/shared/schedules.ts getUpcomingDays
@Suppress("MagicNumber")
fun UpcomingLength.upcomingDays(today: LocalDate): Int {
  val startOfMonth = LocalDate(today.year, today.month, 1)
  val startOfNextMonth = startOfMonth.plus(1, MONTH)
  return when (this) {
    UpcomingLength.CurrentMonth -> startOfNextMonth.minus(1, DAY).day - today.day
    UpcomingLength.OneMonth -> startOfMonth.until(startOfNextMonth, DAY).toInt()
    is UpcomingLength.Days -> count
    is UpcomingLength.Weeks -> count * 7
    is UpcomingLength.Months -> startOfMonth.until(startOfMonth.plus(count, MONTH), DAY).toInt() + 1
    is UpcomingLength.Years ->
      startOfMonth.until(startOfMonth.plus(count * 12, MONTH), DAY).toInt() + 1
  }
}

internal object UpcomingLengthSerializer : KSerializer<UpcomingLength> {
  override val descriptor = PrimitiveSerialDescriptor("UpcomingLength", PrimitiveKind.STRING)

  override fun deserialize(decoder: Decoder): UpcomingLength =
    UpcomingLength.decode(decoder.decodeString())

  override fun serialize(encoder: Encoder, value: UpcomingLength) =
    encoder.encodeString(value.encode())
}
