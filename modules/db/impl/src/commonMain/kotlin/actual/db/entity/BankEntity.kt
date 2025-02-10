package actual.db.entity

import actual.budget.model.BankId
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Entity(tableName = "banks")
data class BankEntity(
  @PrimaryKey @ColumnInfo("id") val id: Uuid = Uuid.random(),
  @ColumnInfo("bank_id") val bankId: BankId?,
  @ColumnInfo("name") val name: String?,
  @ColumnInfo("tombstone") val tombstone: Boolean = false,
)
