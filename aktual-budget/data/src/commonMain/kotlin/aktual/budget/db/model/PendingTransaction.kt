package aktual.budget.db.model

import aktual.budget.model.AccountId
import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
  tableName = "pending_transactions",
  foreignKeys =
    [ForeignKey(entity = Account::class, parentColumns = ["id"], childColumns = ["acct"])],
  indices = [Index("acct")],
)
data class PendingTransaction(
  @PrimaryKey @ColumnInfo(name = "id") val id: String,
  @ColumnInfo(name = "acct") val acct: AccountId?,
  @ColumnInfo(name = "amount") val amount: Int?,
  @ColumnInfo(name = "description") val description: String?,
  @ColumnInfo(name = "date") val date: String?,
)
