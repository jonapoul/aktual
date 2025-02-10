package actual.db.entity

import actual.budget.model.AccountId
import actual.budget.model.CategoryId
import actual.budget.model.PayeeId
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "payees",
  foreignKeys = [
    ForeignKey(AccountEntity::class, parentColumns = ["id"], childColumns = ["transfer_acct"]),
  ],
  indices = [
    Index(value = ["transfer_acct"]),
    Index(value = ["category"]),
  ],
)
data class PayeeEntity(
  @PrimaryKey @ColumnInfo("id") val id: PayeeId,
  @ColumnInfo("name") val name: String?,
  @ColumnInfo("category") val category: CategoryId?,
  @ColumnInfo("tombstone") val tombstone: Boolean = false,
  @ColumnInfo("transfer_acct") val transferAccount: AccountId?,
  @ColumnInfo("favorite") val isFavorite: Boolean? = false,
  @ColumnInfo("learn_categories") val learnCategories: Boolean? = true,
)
