@file:Suppress("JUnitMalformedDeclaration")

package aktual.budget.model

import aktual.budget.model.UpcomingLength.CurrentMonth
import aktual.budget.model.UpcomingLength.Days
import aktual.budget.model.UpcomingLength.Months
import aktual.budget.model.UpcomingLength.OneMonth
import aktual.budget.model.UpcomingLength.Weeks
import aktual.budget.model.UpcomingLength.Years
import app.cash.burst.Burst
import app.cash.burst.burstValues
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDate.Companion.parse
import kotlinx.serialization.json.Json

// packages/loot-core/src/shared/schedules.test.ts getUpcomingDays
@Burst
class UpcomingLengthTest {
  data class UpcomingDaysCase(val length: UpcomingLength, val today: LocalDate, val expected: Int)

  @Test
  fun upcomingDays(
    case: UpcomingDaysCase =
      burstValues(
        UpcomingDaysCase(Days(1), parse("2017-01-01"), 1),
        UpcomingDaysCase(Days(7), parse("2017-01-01"), 7),
        UpcomingDaysCase(Days(14), parse("2017-01-01"), 14),
        UpcomingDaysCase(OneMonth, parse("2017-01-01"), 31),
        UpcomingDaysCase(OneMonth, parse("2017-04-01"), 30),
        UpcomingDaysCase(OneMonth, parse("2017-04-15"), 30),
        UpcomingDaysCase(OneMonth, parse("2017-02-01"), 28),
        UpcomingDaysCase(OneMonth, parse("2020-02-01"), 29), // leap year
        UpcomingDaysCase(CurrentMonth, parse("2017-01-01"), 30),
        UpcomingDaysCase(CurrentMonth, parse("2017-02-01"), 27),
        UpcomingDaysCase(CurrentMonth, parse("2017-02-08"), 20),
        UpcomingDaysCase(CurrentMonth, parse("2020-02-01"), 28), // leap year
        UpcomingDaysCase(Days(2), parse("2017-01-01"), 2),
        UpcomingDaysCase(Weeks(5), parse("2017-01-01"), 35),
        UpcomingDaysCase(Months(3), parse("2017-01-01"), 91),
        UpcomingDaysCase(Years(4), parse("2017-01-01"), 1462),
        UpcomingDaysCase(Years(1), parse("2017-06-15"), 366),
        UpcomingDaysCase(Years(1), parse("2019-06-15"), 367), // leap year
        UpcomingDaysCase(Years(2), parse("2017-06-15"), 731),
      )
  ) {
    assertThat(case.length.upcomingDays(case.today)).isEqualTo(case.expected)
  }

  data class SerializationCase(val length: UpcomingLength, val wire: String)

  @Test
  fun serialize(
    case: SerializationCase =
      burstValues(
        SerializationCase(OneMonth, "oneMonth"),
        SerializationCase(CurrentMonth, "currentMonth"),
        SerializationCase(Days(7), "7-day"),
        SerializationCase(Weeks(5), "5-week"),
        SerializationCase(Months(3), "3-month"),
        SerializationCase(Years(4), "4-year"),
      )
  ) {
    assertThat(encode(case.length)).isEqualTo(case.wire)
  }

  @Test
  fun deserialize(
    case: SerializationCase =
      burstValues(
        SerializationCase(OneMonth, "oneMonth"),
        SerializationCase(CurrentMonth, "currentMonth"),
        SerializationCase(Days(7), "7"), // plain integer format
        SerializationCase(Days(7), "7-day"),
        SerializationCase(Weeks(5), "5-week"),
        SerializationCase(Months(3), "3-month"),
        SerializationCase(Years(4), "4-year"),
      )
  ) {
    assertThat(decode(case.wire)).isEqualTo(case.length)
  }

  private companion object {
    fun encode(value: UpcomingLength): String =
      Json.encodeToString(UpcomingLength.serializer(), value).removeSurrounding("\"")

    fun decode(wire: String): UpcomingLength =
      Json.decodeFromString(UpcomingLength.serializer(), "\"$wire\"")
  }
}
