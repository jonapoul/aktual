package actual.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
  @PrimaryKey @ColumnInfo("id") val id: String,
  @ColumnInfo("note") val note: String?,
)
