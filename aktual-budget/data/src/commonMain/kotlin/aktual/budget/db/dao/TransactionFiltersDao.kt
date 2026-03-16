package aktual.budget.db.dao

import aktual.budget.model.TransactionFilterId
import androidx.room3.Dao
import androidx.room3.Query

@Dao
interface TransactionFiltersDao {
  @Query("SELECT id FROM transaction_filters WHERE tombstone = 0 AND name = :name")
  suspend fun getIdByName(name: String): TransactionFilterId?

  @Query("DELETE FROM transaction_filters WHERE id = :id")
  suspend fun deleteById(id: TransactionFilterId)
}
