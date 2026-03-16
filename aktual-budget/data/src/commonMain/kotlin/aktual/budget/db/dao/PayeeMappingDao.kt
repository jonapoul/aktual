package aktual.budget.db.dao

import aktual.budget.db.model.PayeeMapping
import aktual.budget.model.PayeeId
import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert

@Dao
interface PayeeMappingDao {
  @Upsert suspend fun insert(payeeMapping: PayeeMapping)

  @Query("SELECT id FROM payee_mapping WHERE targetId = :targetId")
  suspend fun getIdsByTargetId(targetId: PayeeId): List<PayeeId>
}
