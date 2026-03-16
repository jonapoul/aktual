package aktual.budget.db.dao

import aktual.budget.db.model.Note
import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert

@Dao
interface NotesDao {
  @Query("SELECT note FROM notes WHERE id = :id") suspend fun getById(id: String): String?

  @Upsert suspend fun insert(note: Note)

  @Query("SELECT * FROM notes WHERE lower(note) LIKE '%#cleanup %'")
  suspend fun getCleanupNotes(): List<Note>
}
