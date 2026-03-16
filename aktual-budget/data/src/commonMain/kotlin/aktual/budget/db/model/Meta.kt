package aktual.budget.db.model

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "__meta__")
data class Meta(
  @PrimaryKey @ColumnInfo(name = "key") val key: String,
  @ColumnInfo(name = "value") val value: String?,
)
