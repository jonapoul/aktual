package actual.db.converter

import actual.db.model.Condition
import androidx.room.TypeConverter

internal object ConditionConverter {
  @TypeConverter
  fun toString(condition: Condition): String = condition.toString()

  @TypeConverter
  fun fromString(string: String): Condition = Condition.fromString(string)
}
