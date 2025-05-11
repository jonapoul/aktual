package actual.db.test

import actual.budget.model.AccountId
import actual.budget.model.AccountSyncSource
import actual.db.Accounts
import kotlin.uuid.Uuid

internal fun buildAccount(
  id: AccountId = AccountId("abc-123"),
  accountId: String = "xyz-789",
  name: String = "John Doe",
  officialName: String = "Jonathan Doe",
  bank: Uuid = BANK_ID,
  offBudget: Boolean = false,
  syncSource: AccountSyncSource = AccountSyncSource.GoCardless,
) = Accounts(
  id = id,
  account_id = accountId,
  name = name,
  balance_current = null,
  balance_available = null,
  balance_limit = null,
  mask = null,
  official_name = officialName,
  subtype = null,
  bank = bank,
  offbudget = offBudget,
  closed = false,
  tombstone = false,
  sort_order = null,
  type = null,
  account_sync_source = syncSource,
  last_sync = null,
  last_reconciled = null,
)
