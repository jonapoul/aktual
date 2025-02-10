package actual.db.converters

import actual.budget.model.ReportDate
import actual.budget.model.Timestamp
import actual.budget.model.TransactionError
import actual.db.model.Condition
import androidx.room.TypeConverter
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Suppress("TooManyFunctions")
internal object OtherConverters {
  @TypeConverter
  fun toInt(value: LocalDate): Int = with(value) { "%04d%02d%02d".format(year, month, dayOfMonth).toInt() }

  @TypeConverter
  fun fromInt(value: Int): LocalDate {
    val str = value.toString()
    val year = str.substring(startIndex = 0, endIndex = 3).toInt()
    val month = str.substring(startIndex = 4, endIndex = 5).toInt()
    val day = str.substring(startIndex = 6, endIndex = 7).toInt()
    return LocalDate(year, month, day)
  }

  @TypeConverter
  fun instantToLong(value: Instant): Long = value.toEpochMilliseconds()

  @TypeConverter
  fun instantFromLong(value: Long): Instant = Instant.fromEpochMilliseconds(value)

  @TypeConverter
  fun listTo(value: List<Condition>): String = Json.encodeToString(ListSerializer(Condition.serializer()), value)

  @TypeConverter
  fun listFrom(value: String): List<Condition> = Json.decodeFromString(ListSerializer(Condition.serializer()), value)

  @TypeConverter
  fun timestampTo(value: Timestamp): String = value.toString()

  @TypeConverter
  fun timestampFrom(value: String): Timestamp = Timestamp.parse(value)

  @TypeConverter
  fun reportDateTo(value: ReportDate): String = value.toString()

  @TypeConverter
  fun reportDateFrom(value: String): ReportDate = ReportDate.parse(value)

  @TypeConverter
  fun transactionErrorTo(value: TransactionError): String = Json.encodeToString(TransactionError.serializer(), value)

  @TypeConverter
  fun transactionErrorFrom(value: String) = Json.decodeFromString(TransactionError.serializer(), value)

  @TypeConverter
  fun uuidTo(value: Uuid): String = value.toString()

  @TypeConverter
  fun uuidFrom(value: String): Uuid = Uuid.parse(value)
}
