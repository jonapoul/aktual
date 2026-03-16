package aktual.budget.db.model

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "__migrations__")
data class Migration(@PrimaryKey @ColumnInfo(name = "id") val id: Int)
