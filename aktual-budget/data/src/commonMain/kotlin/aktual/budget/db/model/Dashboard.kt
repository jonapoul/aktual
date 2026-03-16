package aktual.budget.db.model

import aktual.budget.model.WidgetId
import aktual.budget.model.WidgetType
import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.json.JsonObject

@Entity(tableName = "dashboard")
data class Dashboard(
  @PrimaryKey @ColumnInfo(name = "id") val id: WidgetId,
  @ColumnInfo(name = "type") val type: WidgetType?,
  @ColumnInfo(name = "width") val width: Int?,
  @ColumnInfo(name = "height") val height: Int?,
  @ColumnInfo(name = "x") val x: Int?,
  @ColumnInfo(name = "y") val y: Int?,
  @ColumnInfo(name = "meta") val meta: JsonObject?,
  @ColumnInfo(name = "tombstone") val tombstone: Boolean? = false,
)
