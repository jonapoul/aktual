package dev.jonpoulton.actual.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(
  tableName = "pending_transactions",
  foreignKeys = [
    ForeignKey(
      entity = AccountEntity::class,
      parentColumns = ["id"],
      childColumns = ["acct"],
      onDelete = ForeignKey.CASCADE,
    ),
  ],
  indices = [
    Index(value = ["acct"]),
  ],
)
data class PendingTransactionEntity(
  @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = false) val id: String,
  @ColumnInfo(name = "acct") val account: Int,
  @ColumnInfo(name = "amount") val amount: Int,
  @ColumnInfo(name = "description") val description: String,
  @ColumnInfo(name = "date") val date: String,
)

@Dao
interface PendingTransactionDao {
  @Query("SELECT * FROM pending_transactions")
  suspend fun getAll(): List<PendingTransactionEntity>
}
