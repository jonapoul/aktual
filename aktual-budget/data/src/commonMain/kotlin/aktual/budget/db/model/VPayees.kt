package aktual.budget.db.model

import aktual.budget.model.AccountId
import aktual.budget.model.PayeeId
import androidx.room3.ColumnInfo
import androidx.room3.DatabaseView

@DatabaseView(
  viewName = "v_payees",
  value =
    """
    SELECT
      p.id,
      COALESCE(a.name, p.name) AS name,
      p.transfer_acct,
      p.tombstone,
      p.favorite
    FROM payees p
    LEFT JOIN accounts a ON (p.transfer_acct = a.id AND a.tombstone = 0)
    WHERE p.transfer_acct IS NULL OR a.id IS NOT NULL
  """,
)
data class VPayees(
  @ColumnInfo(name = "id") val id: PayeeId,
  @ColumnInfo(name = "name") val name: String?,
  @ColumnInfo(name = "transfer_acct") val transferAcct: AccountId?,
  @ColumnInfo(name = "tombstone") val tombstone: Boolean?,
  @ColumnInfo(name = "favorite") val favorite: Boolean?,
)
