package actual.db.converter

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDate

internal object LocalDateTypeConverter {
  @TypeConverter
  fun toString(date: LocalDate): String = date.toString()

  @TypeConverter
  fun fromString(string: String): LocalDate = LocalDate.parse(string)
}
