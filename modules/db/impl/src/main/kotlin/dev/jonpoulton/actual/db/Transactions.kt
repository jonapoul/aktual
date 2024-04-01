package dev.jonpoulton.actual.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "transactions")
data class TransactionEntity(
  @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = false) val id: Int,
  @ColumnInfo(name = "isParent") val isParent: Boolean = false,
  @ColumnInfo(name = "isChild") val isChild: Boolean = false,
  @ColumnInfo(name = "acct") val acct: String,
  @ColumnInfo(name = "category") val category: String,
  @ColumnInfo(name = "amount") val amount: Int,
  @ColumnInfo(name = "description") val description: String,
  @ColumnInfo(name = "notes") val notes: String,
  @ColumnInfo(name = "date") val date: Int,
  @ColumnInfo(name = "financial_id") val financialId: String,
  @ColumnInfo(name = "type") val type: String,
  @ColumnInfo(name = "location") val location: String,
  @ColumnInfo(name = "error") val error: String?,
  @ColumnInfo(name = "imported_description") val importedDescription: String,
  @ColumnInfo(name = "starting_balance_flag") val startingBalanceFlag: Int = 0,
  @ColumnInfo(name = "transferred_id") val transferredId: String,
  @ColumnInfo(name = "sort_order") val sortOrder: Float,
  @ColumnInfo(name = "tombstone") val tombstone: Boolean = false,
)

@Dao
interface TransactionDao {
  @Query("SELECT * FROM transactions")
  suspend fun getAll(): List<TransactionEntity>
}
