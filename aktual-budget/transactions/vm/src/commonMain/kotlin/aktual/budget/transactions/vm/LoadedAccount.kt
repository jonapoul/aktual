package aktual.budget.transactions.vm

import aktual.budget.db.Accounts
import aktual.budget.model.AccountGroup
import androidx.compose.runtime.Immutable
import kotlinx.datetime.YearMonth

@Immutable
sealed interface LoadedAccount {
  data object Loading : LoadedAccount

  data object AllAccounts : LoadedAccount

  data object Uncategorised : LoadedAccount

  @JvmInline value class SpecificAccount(val account: Accounts) : LoadedAccount

  @JvmInline value class Group(val group: AccountGroup) : LoadedAccount

  @JvmInline value class SpecificTag(val tag: String) : LoadedAccount

  // A null month covers every month
  data class SpecificCategory(val name: String, val month: YearMonth?) : LoadedAccount
}
