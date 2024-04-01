package dev.jonpoulton.actual.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "banks")
data class BankEntity(
  @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = false) val id: String,
  @ColumnInfo(name = "bank_id") val bankId: String,
  @ColumnInfo(name = "name") val name: String,
  @ColumnInfo(name = "tombstone") val tombstone: Boolean = false,
)

@Dao
interface BankDao {
  @Query("SELECT * FROM banks")
  suspend fun getAll(): List<BankEntity>
}
