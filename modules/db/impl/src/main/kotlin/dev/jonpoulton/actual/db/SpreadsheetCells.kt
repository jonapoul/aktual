package dev.jonpoulton.actual.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "spreadsheet_cells")
data class SpreadsheetCellEntity(
  @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = false) val id: String,
  @ColumnInfo(name = "expr") val expr: String,
  @ColumnInfo(name = "cachedValue") val cachedValue: String,
)

@Dao
interface SpreadsheetCellDao {
  @Query("SELECT * FROM spreadsheet_cells")
  suspend fun getAll(): List<SpreadsheetCellEntity>
}
