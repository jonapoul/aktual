package aktual.budget.db.dao

import aktual.budget.db.model.Transaction
import aktual.budget.db.model.TransactionView
import aktual.budget.model.AccountId
import aktual.budget.model.TransactionId
import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionsDao {
  @Insert suspend fun insert(transaction: Transaction)

  @Query(
    """
    SELECT vt.id, vt.date, a.name AS accountName, p.name AS payeeName, vt.notes, c.name AS categoryName, vt.amount
    FROM v_transactions_internal_alive AS vt
    LEFT JOIN categories AS c ON (c.id = vt.category AND c.tombstone = 0)
    LEFT JOIN accounts AS a ON (a.id = vt.account AND a.tombstone = 0)
    LEFT JOIN payees AS p ON (p.id = vt.payee AND p.tombstone = 0)
    WHERE vt.id = :id
    """
  )
  fun observeById(id: TransactionId): Flow<TransactionView?>

  @Query("SELECT id FROM v_transactions ORDER BY date DESC LIMIT :limit OFFSET :offset")
  suspend fun getIdsPaged(limit: Long, offset: Long): List<TransactionId>

  @Query(
    "SELECT id FROM v_transactions WHERE account = :account ORDER BY date DESC LIMIT :limit OFFSET :offset"
  )
  suspend fun getIdsByAccountPaged(
    account: AccountId,
    limit: Long,
    offset: Long,
  ): List<TransactionId>

  @Query("SELECT COUNT(*) FROM v_transactions") fun observeCount(): Flow<Long>

  @Query("SELECT COUNT(*) FROM v_transactions WHERE account = :account")
  fun observeCountByAccount(account: AccountId): Flow<Long>

  @Query("SELECT id FROM v_transactions WHERE account = :account ORDER BY date DESC")
  suspend fun getIdsByAccount(account: AccountId): List<TransactionId>

  @Query("SELECT id FROM v_transactions ORDER BY date DESC")
  suspend fun getIds(): List<TransactionId>
}
