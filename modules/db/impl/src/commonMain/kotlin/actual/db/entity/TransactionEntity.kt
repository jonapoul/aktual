package actual.db.entity

import actual.budget.model.AccountId
import actual.budget.model.CategoryId
import actual.budget.model.PayeeId
import actual.budget.model.ScheduleId
import actual.budget.model.TransactionError
import actual.budget.model.TransactionId
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Index.Order.ASC
import androidx.room.Index.Order.DESC
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(
  tableName = "transactions",
  indices = [
    Index(value = ["category", "date"], name = "trans_category_date"),
    Index(value = ["category"], name = "trans_category"),
    Index(value = ["date"], name = "trans_date"),
    Index(value = ["parent_id"], name = "trans_parent_id"),
    Index(
      name = "trans_sorted",
      value = ["date", "starting_balance_flag", "sort_order", "id"],
      orders = [DESC, ASC, DESC, ASC],
    ),
    Index(value = ["acct"]),
  ],
  foreignKeys = [
    ForeignKey(AccountEntity::class, parentColumns = ["id"], childColumns = ["acct"]),
    ForeignKey(CategoryEntity::class, parentColumns = ["id"], childColumns = ["category"]),
  ],
)
data class TransactionEntity(
  @PrimaryKey @ColumnInfo("id") val id: TransactionId,
  @ColumnInfo("isParent") val isParent: Boolean? = false,
  @ColumnInfo("isChild") val isChild: Boolean? = false,
  @ColumnInfo("acct") val account: AccountId?,
  @ColumnInfo("category") val category: CategoryId?,
  @ColumnInfo("amount") val amount: Int?,
  @ColumnInfo("description") val payee: PayeeId?,
  @ColumnInfo("notes") val notes: String?,
  @ColumnInfo("date") val date: LocalDate?,
  @ColumnInfo("financial_id") val financialId: Int?,
  @ColumnInfo("type") val type: String?,
  @ColumnInfo("location") val location: String?,
  @ColumnInfo("error") val error: TransactionError?,
  @ColumnInfo("imported_description") val importedPayee: PayeeId?,
  @ColumnInfo("starting_balance_flag") val isStartingBalance: Boolean? = false,
  @ColumnInfo("transferred_id") val transferredId: TransactionId?,
  @ColumnInfo("sort_order") val sortOrder: Float?,
  @ColumnInfo("tombstone") val tombstone: Boolean = false,
  @ColumnInfo("cleared") val isCleared: Boolean? = true,
  @ColumnInfo("pending") val isPending: Boolean? = false,
  @ColumnInfo("parent_id") val parentId: TransactionId?,
  @ColumnInfo("schedule") val schedule: ScheduleId?,
  @ColumnInfo("reconciled") val reconciled: Boolean? = false,
)
