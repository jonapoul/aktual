package aktual.budget.db.model

import aktual.budget.model.AccountId
import aktual.budget.model.PayeeId
import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "payees")
data class Payee(
  @PrimaryKey @ColumnInfo(name = "id") val id: PayeeId,
  @ColumnInfo(name = "name") val name: String?,
  @ColumnInfo(name = "category") val category: String?,
  @ColumnInfo(name = "tombstone") val tombstone: Boolean? = false,
  @ColumnInfo(name = "transfer_acct") val transferAcct: AccountId?,
  @ColumnInfo(name = "favorite") val favorite: Boolean? = false,
  @ColumnInfo(name = "learn_categories") val learnCategories: Int? = 1,
)
