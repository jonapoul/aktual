package aktual.budget.db.dao

import aktual.budget.db.converters.InstantMsFromStringConverter
import aktual.budget.db.model.Account
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.TypeConverters
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow

@Dao
@TypeConverters(InstantMsFromStringConverter::class)
interface AccountsDao {
  @Insert suspend fun insert(account: Account)

  @Insert suspend fun insert(account: List<Account>)

  @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
  fun observeById(id: AccountId): Flow<Account?>

  @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
  suspend fun getById(id: AccountId): Account?

  @Query("SELECT * FROM accounts WHERE id = :id AND tombstone = 0 LIMIT 1")
  suspend fun getFirstNotTombstoned(id: AccountId): Account?

  @Query("UPDATE accounts SET balance_current = :balance WHERE id = :id")
  suspend fun updateBalance(id: AccountId, balance: Amount)

  @Query("UPDATE accounts SET closed = 1 WHERE id = :id") suspend fun closeAccount(id: AccountId)

  @Query("UPDATE accounts SET closed = 0 WHERE id = :id") suspend fun openAccount(id: AccountId)

  @Query("UPDATE accounts SET last_sync = :lastSync WHERE id = :id")
  suspend fun updateSyncTime(id: AccountId, lastSync: Instant)

  @Query(
    "SELECT SUM(amount) AS balance FROM transactions WHERE acct = :id AND isParent = 0 AND tombstone = 0 AND date <= :date"
  )
  suspend fun getAccountBalance(id: AccountId, date: Long): Long?
}
