package aktual.budget.db.dao

import aktual.budget.db.model.Payee
import aktual.budget.model.AccountId
import aktual.budget.model.PayeeId
import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert

@Dao
interface PayeesDao {
  @Query("SELECT id FROM payees WHERE transfer_acct = :accountId")
  suspend fun getIdByTransferAccount(accountId: AccountId): PayeeId?

  @Query("SELECT id FROM payees WHERE LOWER(name) = :name AND tombstone = 0")
  suspend fun getIdByName(name: String): PayeeId?

  @Query("SELECT * FROM payees WHERE id = :id LIMIT 1") suspend fun getById(id: PayeeId): Payee?

  @Upsert suspend fun insert(payee: Payee)

  @Query("DELETE FROM payees WHERE id = :id") suspend fun deleteById(id: PayeeId)
}
