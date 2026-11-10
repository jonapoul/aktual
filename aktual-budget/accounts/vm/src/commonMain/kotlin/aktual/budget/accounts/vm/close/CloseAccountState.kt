package aktual.budget.accounts.vm.close

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import androidx.compose.runtime.Immutable
import kotlin.jvm.JvmInline
import kotlinx.collections.immutable.ImmutableList

@Immutable sealed interface CloseAccountState

data object Loading : CloseAccountState

@JvmInline value class Failure(val message: String) : CloseAccountState

data class Ready(
  val name: String,
  val balance: Amount,
  val transactionCount: Long,
  // Transfers to other accounts, which deleting turns into ordinary transactions
  val transferCount: Int,
  val isLinked: Boolean,
  val case: CloseCase,
  // Open accounts the balance can go to
  val targets: ImmutableList<TransferTarget>,
  val transferTo: AccountId?,
  // Moving the balance from on budget to off budget needs a category, as upstream's needsCategory()
  val needsCategory: Boolean,
  val category: CategoryId?,
  val categoryName: String?,
  val canClose: Boolean,
  val isWorking: Boolean,
  val error: String?,
) : CloseAccountState

enum class CloseCase {
  // A balance that has to go to another account first
  HasBalance,
  ZeroBalance,
  // Closing deletes it
  NoTransactions,
}

@Immutable data class TransferTarget(val id: AccountId, val name: String, val offBudget: Boolean)
