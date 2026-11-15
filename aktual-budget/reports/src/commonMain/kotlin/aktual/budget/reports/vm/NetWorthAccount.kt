package aktual.budget.reports.vm

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.datetime.LocalDate

// Keyed by the first day of each period
@Immutable
data class NetWorthAccount(
  val id: AccountId,
  val name: String,
  val balances: ImmutableMap<LocalDate, Amount>,
)
