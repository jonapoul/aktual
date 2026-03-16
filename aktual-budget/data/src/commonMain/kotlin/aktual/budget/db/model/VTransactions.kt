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
  viewName = "v_transactions",
  value =
    """
    SELECT
      vtia.id,
      vtia.is_parent,
      vtia.is_child,
      vtia.parent_id,
      a.id AS account,
      c.id AS category,
      vtia.amount,
      p.id AS payee,
      vtia.notes,
      vtia.date,
      vtia.imported_id,
      vtia.error,
      vtia.imported_payee,
      vtia.starting_balance_flag,
      vtia.transfer_id,
      vtia.sort_order,
      vtia.cleared,
      vtia.reconciled,
      vtia.tombstone,
      vtia.schedule
    FROM v_transactions_internal_alive vtia
    LEFT JOIN payees p ON (p.id = vtia.payee AND p.tombstone = 0)
    LEFT JOIN categories c ON (c.id = vtia.category AND c.tombstone = 0)
    LEFT JOIN accounts a ON (a.id = vtia.account AND a.tombstone = 0)
    ORDER BY
      vtia.date DESC,
      vtia.starting_balance_flag,
      vtia.sort_order DESC,
      vtia.id
  """,
)
data class VTransactions(
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
