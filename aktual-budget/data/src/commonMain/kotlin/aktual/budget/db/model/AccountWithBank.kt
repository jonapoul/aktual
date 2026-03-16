package aktual.budget.db.model

import aktual.budget.db.converters.InstantMsFromStringConverter
import aktual.budget.model.AccountId
import aktual.budget.model.AccountSyncSource
import aktual.budget.model.Amount
import androidx.room3.ColumnInfo
import androidx.room3.TypeConverters
import kotlin.time.Instant
import kotlin.uuid.Uuid

@TypeConverters(InstantMsFromStringConverter::class)
data class AccountWithBank(
  @ColumnInfo(name = "id") val id: AccountId,
  @ColumnInfo(name = "account_id") val accountId: String?,
  @ColumnInfo(name = "name") val name: String?,
  @ColumnInfo(name = "balance_current") val balanceCurrent: Amount?,
  @ColumnInfo(name = "balance_available") val balanceAvailable: Amount?,
  @ColumnInfo(name = "balance_limit") val balanceLimit: Amount?,
  @ColumnInfo(name = "mask") val mask: String?,
  @ColumnInfo(name = "official_name") val officialName: String?,
  @ColumnInfo(name = "subtype") val subtype: String?,
  @ColumnInfo(name = "bank") val bank: Uuid?,
  @ColumnInfo(name = "offbudget") val offBudget: Boolean?,
  @ColumnInfo(name = "closed") val closed: Boolean?,
  @ColumnInfo(name = "tombstone") val tombstone: Boolean?,
  @ColumnInfo(name = "sort_order") val sortOrder: Double?,
  @ColumnInfo(name = "type") val type: String?,
  @ColumnInfo(name = "account_sync_source") val accountSyncSource: AccountSyncSource?,
  @ColumnInfo(name = "last_sync") val lastSync: Instant?,
  @ColumnInfo(name = "last_reconciled") val lastReconciled: Instant?,
  @ColumnInfo(name = "bankName") val bankName: String?,
  @ColumnInfo(name = "bankId") val bankId: Uuid?,
)
