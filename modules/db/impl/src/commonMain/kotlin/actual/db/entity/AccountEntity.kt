package actual.db.entity

import actual.budget.model.AccountId
import actual.budget.model.AccountSyncSource
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
  @PrimaryKey @ColumnInfo("id") val id: AccountId,
  @ColumnInfo("account_id") val accountId: String?,
  @ColumnInfo("name") val name: String?,
  @ColumnInfo("balance_current") val balanceCurrent: Int?,
  @ColumnInfo("balance_available") val balanceAvailable: Int?,
  @ColumnInfo("balance_limit") val balanceLimit: Int?,
  @ColumnInfo("mask") val mask: String?,
  @ColumnInfo("official_name") val officialName: String?,
  @ColumnInfo("subtype") val subtype: String?,
  @ColumnInfo("bank") val bank: String?,
  @ColumnInfo("offbudget") val offBudget: Boolean = false,
  @ColumnInfo("closed") val closed: Boolean = false,
  @ColumnInfo("tombstone") val tombstone: Boolean = false,
  @ColumnInfo("sort_order") val sortOrder: Float?,
  @ColumnInfo("type") val type: String?,
  @ColumnInfo("account_sync_source") val syncSource: AccountSyncSource?,
)
