package aktual.budget.db.model

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.model.ScheduleId
import aktual.budget.model.TransactionId
import androidx.room3.ColumnInfo
import androidx.room3.DatabaseView
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonObject

@DatabaseView(
  viewName = "v_transactions_internal_alive",
  value =
    """
    SELECT
      vti.*
    FROM v_transactions_internal vti
    LEFT JOIN transactions t2 ON (vti.is_child = 1 AND t2.id = vti.parent_id)
    WHERE IFNULL(vti.tombstone, 0) = 0 AND (vti.is_child = 0 OR t2.tombstone = 0)
  """,
)
data class VTransactionsInternalAlive(
  @ColumnInfo(name = "id") val id: TransactionId,
  @ColumnInfo(name = "is_parent") val isParent: Boolean?,
  @ColumnInfo(name = "is_child") val isChild: Boolean?,
  @ColumnInfo(name = "parent_id") val parentId: TransactionId?,
  @ColumnInfo(name = "account") val account: AccountId?,
  @ColumnInfo(name = "category") val category: CategoryId?,
  @ColumnInfo(name = "amount") val amount: Amount,
  @ColumnInfo(name = "payee") val payee: PayeeId?,
  @ColumnInfo(name = "notes") val notes: String?,
  @ColumnInfo(name = "date") val date: LocalDate?,
  @ColumnInfo(name = "imported_id") val importedId: String?,
  @ColumnInfo(name = "error") val error: JsonObject?,
  @ColumnInfo(name = "imported_payee") val importedPayee: PayeeId?,
  @ColumnInfo(name = "starting_balance_flag") val startingBalanceFlag: Boolean?,
  @ColumnInfo(name = "transfer_id") val transferId: TransactionId?,
  @ColumnInfo(name = "sort_order") val sortOrder: Double?,
  @ColumnInfo(name = "cleared") val cleared: Boolean?,
  @ColumnInfo(name = "reconciled") val reconciled: Boolean?,
  @ColumnInfo(name = "tombstone") val tombstone: Boolean?,
  @ColumnInfo(name = "schedule") val schedule: ScheduleId?,
)
