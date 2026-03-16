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
  viewName = "v_transactions_internal",
  value =
    """
    SELECT
      t.id,
      t.isParent AS is_parent,
      t.isChild AS is_child,
      CASE WHEN t.isChild = 0 THEN NULL ELSE t.parent_id END AS parent_id,
      t.acct AS account,
      CASE WHEN t.isParent = 1 THEN NULL ELSE cm.transferId END AS category,
      IFNULL(t.amount, 0) AS amount,
      pm.targetId AS payee,
      t.notes,
      t.date,
      t.financial_id AS imported_id,
      t.error,
      t.imported_description AS imported_payee,
      t.starting_balance_flag,
      t.transferred_id AS transfer_id,
      t.sort_order,
      t.cleared,
      t.reconciled,
      t.tombstone,
      t.schedule
    FROM transactions t
    LEFT JOIN category_mapping cm ON cm.id = t.category
    LEFT JOIN payee_mapping pm ON pm.id = t.description
    WHERE
      t.date IS NOT NULL AND
      t.acct IS NOT NULL AND
      (t.isChild = 0 OR t.parent_id IS NOT NULL)
  """,
)
data class VTransactionsInternal(
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
