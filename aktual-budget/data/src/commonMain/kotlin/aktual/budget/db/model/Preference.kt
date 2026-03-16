package aktual.budget.db.model

import aktual.budget.model.SyncedPrefKey
import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "preferences")
data class Preference(
  @PrimaryKey @ColumnInfo(name = "id") val id: SyncedPrefKey,
  @ColumnInfo(name = "value") val value: String?,
)
