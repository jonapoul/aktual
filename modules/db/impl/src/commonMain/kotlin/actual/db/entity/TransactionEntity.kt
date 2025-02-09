package actual.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Index.Order.ASC
import androidx.room.Index.Order.DESC
import androidx.room.PrimaryKey

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
  ],
)
data class TransactionEntity(
  @PrimaryKey @ColumnInfo("id") val id: String,
  @ColumnInfo("isParent") val isParent: Boolean? = false,
  @ColumnInfo("isChild") val isChild: Boolean? = false,
  @ColumnInfo("acct") val account: String?,
  @ColumnInfo("category") val category: String?,
  @ColumnInfo("amount") val amount: Int?,
  @ColumnInfo("description") val description: String?,
  @ColumnInfo("notes") val notes: String?,
  @ColumnInfo("date") val date: Int?,
  @ColumnInfo("financial_id") val financialId: Int?,
  @ColumnInfo("type") val type: String?,
  @ColumnInfo("location") val location: String?,
  @ColumnInfo("error") val error: String?,
  @ColumnInfo("imported_description") val importedDescription: String?,
  @ColumnInfo("starting_balance_flag") val startingBalanceFlag: Boolean? = false,
  @ColumnInfo("transferred_id") val transferredId: String?,
  @ColumnInfo("sort_order") val sortOrder: Float?,
  @ColumnInfo("tombstone") val tombstone: Boolean? = false,
  @ColumnInfo("cleared") val isCleared: Boolean? = true,
  @ColumnInfo("pending") val isPending: Boolean? = false,
  @ColumnInfo("parent_id") val parentId: String?,
  @ColumnInfo("schedule") val schedule: String?,
  @ColumnInfo("reconciled") val reconciled: Int? = 0,
)
