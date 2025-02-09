package actual.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
  tableName = "pending_transactions",
  foreignKeys = [
    ForeignKey(
      entity = AccountEntity::class,
      parentColumns = ["id"],
      childColumns = ["acct"],
      onDelete = ForeignKey.NO_ACTION,
      onUpdate = ForeignKey.NO_ACTION,
    ),
  ],
)
data class PendingTransactionEntity(
  @PrimaryKey @ColumnInfo("id") val id: String,
  @ColumnInfo("acct", index = true) val accountId: Int?,
  @ColumnInfo("amount") val amount: Int?,
  @ColumnInfo("description") val description: String?,
  @ColumnInfo("date") val date: String?,
)
