package aktual.budget.db.model

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.json.JsonObject

@Entity(tableName = "messages_clock")
data class MessagesClock(
  @PrimaryKey @ColumnInfo(name = "id") val id: Int,
  @ColumnInfo(name = "clock") val clock: JsonObject?,
)
