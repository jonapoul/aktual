package aktual.budget.model

import androidx.compose.runtime.Immutable

@Immutable
data class TransactionsSpec(
  val accountSpec: AccountSpec = AllAccounts,
  val tagSpec: TagSpec = AllTags,
)

sealed interface AccountSpec {
  data object AllAccounts : AccountSpec

  data class SpecificAccount(val id: AccountId) : AccountSpec
}

sealed interface TagSpec {
  data object AllTags : TagSpec

  data class SpecificTag(val id: TagId) : TagSpec
}
