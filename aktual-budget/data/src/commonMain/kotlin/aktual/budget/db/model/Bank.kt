package aktual.budget.db.model

import aktual.budget.model.BankId
import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlin.uuid.Uuid

@Entity(tableName = "banks")
data class Bank(
  @PrimaryKey @ColumnInfo(name = "id") val id: Uuid,
  @ColumnInfo(name = "bank_id") val bankId: BankId?,
  @ColumnInfo(name = "name") val name: String?,
  @ColumnInfo(name = "tombstone") val tombstone: Boolean? = false,
)
