package aktual.budget.db.converters

import aktual.budget.model.ReportCondition
import aktual.budget.model.SelectedCategory
import androidx.room3.TypeConverter
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

internal class JsonElementConverter {
  @TypeConverter fun from(value: String): JsonElement = Json.parseToJsonElement(value)

  @TypeConverter fun to(value: JsonElement): String = Json.encodeToString(value)
}

internal class JsonObjectConverter {
  @TypeConverter fun from(value: String): JsonObject = Json.parseToJsonElement(value).jsonObject

  @TypeConverter fun to(value: JsonObject): String = Json.encodeToString(value)
}

internal class JsonArrayConverter {
  @TypeConverter fun from(value: String): JsonArray = Json.parseToJsonElement(value).jsonArray

  @TypeConverter fun to(value: JsonArray): String = Json.encodeToString(value)
}

internal class ReportConditionListConverter {
  @TypeConverter
  fun from(value: String): List<ReportCondition> =
    Json.decodeFromString(ReportCondition.ListSerializer, value)

  @TypeConverter
  fun to(value: List<ReportCondition>): String =
    Json.encodeToString(ReportCondition.ListSerializer, value)
}

internal class SelectedCategoryListConverter {
  @TypeConverter
  fun from(value: String): List<SelectedCategory> =
    Json.decodeFromString(ListSerializer(SelectedCategory.serializer()), value)

  @TypeConverter
  fun to(value: List<SelectedCategory>): String =
    Json.encodeToString(ListSerializer(SelectedCategory.serializer()), value)
}
