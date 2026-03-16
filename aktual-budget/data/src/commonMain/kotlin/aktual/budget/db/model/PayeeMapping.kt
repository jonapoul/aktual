package aktual.budget.db.model

import aktual.budget.model.PayeeId
import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "payee_mapping")
data class PayeeMapping(
  @PrimaryKey @ColumnInfo(name = "id") val id: PayeeId,
  @ColumnInfo(name = "targetId") val targetId: PayeeId?,
)
