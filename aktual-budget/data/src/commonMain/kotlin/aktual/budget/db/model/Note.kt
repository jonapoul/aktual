package aktual.budget.db.model

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "notes")
data class Note(
  @PrimaryKey @ColumnInfo(name = "id") val id: String,
  @ColumnInfo(name = "note") val note: String?,
)
