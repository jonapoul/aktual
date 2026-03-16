package aktual.budget.db.model

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.model.ScheduleId
import aktual.budget.model.TransactionId
import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonObject

@Entity(
  tableName = "transactions",
  indices =
    [
      androidx.room3.Index(value = ["category", "date"], name = "trans_category_date"),
      androidx.room3.Index(value = ["category"], name = "trans_category"),
      androidx.room3.Index(value = ["date"], name = "trans_date"),
      androidx.room3.Index(value = ["parent_id"], name = "trans_parent_id"),
      androidx.room3.Index(
        value = ["date", "starting_balance_flag", "sort_order", "id"],
        name = "trans_sorted",
      ),
    ],
)
data class Transaction(
  @PrimaryKey @ColumnInfo(name = "id") val id: TransactionId,
  @ColumnInfo(name = "isParent") val isParent: Boolean? = false,
  @ColumnInfo(name = "isChild") val isChild: Boolean? = false,
  @ColumnInfo(name = "acct") val acct: AccountId?,
  @ColumnInfo(name = "category") val category: CategoryId?,
  @ColumnInfo(name = "amount") val amount: Amount?,
  @ColumnInfo(name = "description") val description: PayeeId?,
  @ColumnInfo(name = "notes") val notes: String?,
  @ColumnInfo(name = "date") val date: LocalDate?,
  @ColumnInfo(name = "financial_id") val financialId: String?,
  @ColumnInfo(name = "type") val type: String?,
  @ColumnInfo(name = "location") val location: String?,
  @ColumnInfo(name = "error") val error: JsonObject?,
  @ColumnInfo(name = "imported_description") val importedDescription: PayeeId?,
  @ColumnInfo(name = "starting_balance_flag") val startingBalanceFlag: Boolean? = false,
  @ColumnInfo(name = "transferred_id") val transferredId: TransactionId?,
  @ColumnInfo(name = "sort_order") val sortOrder: Double?,
  @ColumnInfo(name = "tombstone") val tombstone: Boolean? = false,
  @ColumnInfo(name = "cleared") val cleared: Boolean? = true,
  @ColumnInfo(name = "pending") val pending: Boolean? = false,
  @ColumnInfo(name = "parent_id") val parentId: TransactionId?,
  @ColumnInfo(name = "schedule") val schedule: ScheduleId?,
  @ColumnInfo(name = "reconciled") val reconciled: Boolean? = false,
  @ColumnInfo(name = "raw_synced_data") val rawSyncedData: String?,
)
