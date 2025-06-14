package actual.budget.transactions.vm

import androidx.compose.runtime.Immutable

@Immutable
sealed interface SortBy {
  val ascending: Boolean

  data class Date(override val ascending: Boolean) : SortBy
  data class Account(override val ascending: Boolean) : SortBy
  data class Payee(override val ascending: Boolean) : SortBy
  data class Notes(override val ascending: Boolean) : SortBy
  data class Category(override val ascending: Boolean) : SortBy
  data class Amount(override val ascending: Boolean) : SortBy
}
