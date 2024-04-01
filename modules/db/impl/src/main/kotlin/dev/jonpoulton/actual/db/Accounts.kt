package dev.jonpoulton.actual.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "accounts")
data class AccountEntity(
  @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = false) val id: String,
  @ColumnInfo(name = "name") val name: String?,
  @ColumnInfo(name = "balance_current") val currentBalance: Int?,
  @ColumnInfo(name = "balance_available") val availableBalance: Int?,
  @ColumnInfo(name = "balance_limit") val limitBalance: Int?,
  @ColumnInfo(name = "mask") val mask: String?,
  @ColumnInfo(name = "official_name") val officialName: String?,
  @ColumnInfo(name = "type") val type: String?,
  @ColumnInfo(name = "subtype") val subtype: String?,
  @ColumnInfo(name = "bank") val bank: String?,
  @ColumnInfo(name = "offbudget") val offBudget: Boolean = false,
  @ColumnInfo(name = "closed") val closed: Boolean = false,
  @ColumnInfo(name = "tombstone") val tombstone: Boolean = false,
)

@Dao
interface AccountDao {
  @Query("SELECT * FROM accounts")
  suspend fun getAll(): List<AccountEntity>
}
