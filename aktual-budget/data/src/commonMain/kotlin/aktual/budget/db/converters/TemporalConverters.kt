package aktual.budget.db.converters

import androidx.room3.TypeConverter
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.number

internal class InstantMsFromStringConverter {
  @TypeConverter fun from(value: String): Instant = Instant.fromEpochMilliseconds(value.toLong())

  @TypeConverter fun to(value: Instant): String = value.toEpochMilliseconds().toString()
}

internal class InstantFromLongConverter {
  @TypeConverter fun from(value: Long): Instant = Instant.fromEpochMilliseconds(value)

  @TypeConverter fun to(value: Instant): Long = value.toEpochMilliseconds()
}

internal class LocalDateConverter {
  @TypeConverter
  fun from(value: Long): LocalDate {
    val str = value.toString()
    val year = str.substring(startIndex = 0, endIndex = 4).toInt()
    val month = str.substring(startIndex = 4, endIndex = 6).toInt()
    val day = str.substring(startIndex = 6, endIndex = 8).toInt()
    return LocalDate(year, month, day)
  }

  @TypeConverter
  fun to(value: LocalDate): Long =
    with(value) { "%04d%02d%02d".format(year, month.number, day).toLong() }
}

private const val YEAR_MONTH_FACTOR = 100

internal class YearMonthFromLongConverter {
  @TypeConverter
  fun from(value: Long): YearMonth {
    val month = Month(value.toInt() % YEAR_MONTH_FACTOR)
    val year = value.toInt() / YEAR_MONTH_FACTOR
    return YearMonth(year, month)
  }

  @TypeConverter
  fun to(value: YearMonth): Long = value.year * YEAR_MONTH_FACTOR.toLong() + value.month.number
}

internal class YearMonthFromStringConverter {
  @TypeConverter fun from(value: String): YearMonth = YearMonth.parse(value)

  @TypeConverter fun to(value: YearMonth): String = value.toString()
}
