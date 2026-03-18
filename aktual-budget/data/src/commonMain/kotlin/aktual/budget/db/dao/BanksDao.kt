package aktual.budget.db.dao

import aktual.budget.db.model.AccountWithBank
import aktual.budget.db.model.Bank
import aktual.budget.model.BankId
import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import kotlin.uuid.Uuid

@Dao
interface BanksDao {
  @Query("SELECT bank_id FROM banks WHERE id = :id") suspend fun getBankId(id: Uuid): BankId?

  @Query("SELECT * FROM banks WHERE bank_id = :bankId")
  suspend fun getByBankId(bankId: BankId): Bank?

  @Insert suspend fun insert(bank: Bank)

  @Insert suspend fun insert(bank: List<Bank>)

  @Query(
    """
    SELECT a.*, b.name AS bankName, b.id AS bankId
    FROM accounts a
    LEFT JOIN banks b ON a.bank = b.id
    WHERE a.tombstone = 0
    ORDER BY sort_order, a.name
    """
  )
  suspend fun getAccountsWithBank(): List<AccountWithBank>
}
