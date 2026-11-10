package aktual.budget.accounts.vm.manage

import aktual.budget.model.AccountGroupId
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import androidx.compose.runtime.Immutable
import kotlin.jvm.JvmInline
import kotlinx.collections.immutable.ImmutableList

@Immutable sealed interface ManageAccountsState

data object Loading : ManageAccountsState

@JvmInline value class Failure(val message: String) : ManageAccountsState

/**
 * Open accounts split into on and off budget, each with Ungrouped first then every group in order,
 * empty ones included, so either kind of account can be dragged into any group.
 */
data class Success(
  val onBudget: AccountSection,
  val offBudget: AccountSection,
  val groups: ImmutableList<AccountGroupItem>,
  val closed: ImmutableList<ManagedAccount>,
  val closedExpanded: Boolean,
) : ManageAccountsState

// Group names are unique ignoring case, as AccountWriter checks
fun Success.isGroupNameTaken(name: String, except: AccountGroupId? = null): Boolean = groups.any {
  it.id != except && it.name.equals(name.trim(), ignoreCase = true)
}

@Immutable data class AccountGroupItem(val id: AccountGroupId, val name: String)

@Immutable data class AccountSection(val total: Amount, val groups: ImmutableList<ManagedGroup>)

// A null id is Ungrouped, which has no name of its own
@Immutable
data class ManagedGroup(
  val id: AccountGroupId?,
  val name: String,
  val total: Amount,
  val accounts: ImmutableList<ManagedAccount>,
)

@Immutable
data class ManagedAccount(
  val id: AccountId,
  val name: String,
  val balance: Amount,
  val transactionCount: Long,
  val hasNotes: Boolean,
  val offBudget: Boolean,
  val group: AccountGroupId?,
)
