package aktual.budget.model

import androidx.compose.runtime.Immutable

@Immutable
data class TransactionsSpec(
  val accountSpec: AccountSpec = AllAccounts,
  val tagSpec: TagSpec = AllTags,
  val categorySpec: CategorySpec = AllCategories,
)

sealed interface AccountSpec {
  data object AllAccounts : AccountSpec

  data class SpecificAccount(val id: AccountId) : AccountSpec
}

sealed interface TagSpec {
  data object AllTags : TagSpec

  data class SpecificTag(val id: TagId) : TagSpec
}

sealed interface CategorySpec {
  data object AllCategories : CategorySpec

  data object Uncategorised : CategorySpec
}
