package aktual.budget.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.serialization.Serializable

@Immutable
data class TransactionsSpec(
  val accountSpec: AccountSpec = AllAccounts,
  val tagSpec: TagSpec = AllTags,
  val categorySpec: CategorySpec = AllCategories,
)

sealed interface AccountSpec {
  data object AllAccounts : AccountSpec

  data class SpecificAccount(val id: AccountId) : AccountSpec

  data class Group(val group: AccountGroup) : AccountSpec
}

// The account lists of upstream's sidebar. Closed accounts are in neither budget group
@Serializable
enum class AccountGroup {
  OnBudget,
  OffBudget,
  Closed,
}

sealed interface TagSpec {
  data object AllTags : TagSpec

  data class SpecificTag(val id: TagId) : TagSpec
}

sealed interface CategorySpec {
  data object AllCategories : CategorySpec

  data object Uncategorised : CategorySpec

  // A null month covers every month
  data class SpecificCategory(val id: CategoryId, val month: YearMonth? = null) : CategorySpec {
    val dates: ClosedRange<LocalDate>
      get() = if (month == null) ALL_DATES else month.firstDay..month.lastDay
  }
}

private val ALL_DATES = LocalDate(1, 1, 1)..LocalDate(9999, 12, 31)
