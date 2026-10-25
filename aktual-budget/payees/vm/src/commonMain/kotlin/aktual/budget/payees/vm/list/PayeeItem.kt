package aktual.budget.payees.vm.list

import aktual.budget.model.PayeeId
import androidx.compose.runtime.Immutable

@Immutable
data class PayeeItem(
  val id: PayeeId,
  val name: String,
  val isTransfer: Boolean,
  val isFavorite: Boolean,
  val numTransactions: Int,
  val numRules: Int,
  // No transactions and not in a legacy description rule, as upstream's orphaned payees
  val isUnused: Boolean,
)
