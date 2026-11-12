package aktual.budget.transactions.vm

import aktual.budget.model.AccountId
import androidx.compose.runtime.Immutable

// An open account the title bar can switch the list to
@Immutable data class AccountChoice(val id: AccountId, val name: String?)
