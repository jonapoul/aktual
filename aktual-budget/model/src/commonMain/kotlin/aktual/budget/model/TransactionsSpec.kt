package aktual.budget.model

import androidx.compose.runtime.Immutable

@Immutable
data class TransactionsSpec(
  val accountSpec: AccountSpec = AccountSpec.AllAccounts,
  val tagSpec: TagSpec = TagSpec.AllTags,
)

@Immutable
sealed interface AccountSpec {
  data object AllAccounts : AccountSpec

  data class SpecificAccount(val id: AccountId) : AccountSpec
}

@Immutable
sealed interface TagSpec {
  data object AllTags : TagSpec

  data class SpecificTag(val id: TagId) : TagSpec
}
