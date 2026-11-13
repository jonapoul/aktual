package aktual.budget.model

import androidx.compose.runtime.Immutable

/** Narrows an account's list to the rows matching [text] and [conditions] */
@Immutable
data class TransactionSearch(
  val text: String = "",
  val conditions: ConditionSet = ConditionSet(emptyList()),
) {
  val isEmpty: Boolean
    get() = text.isBlank() && conditions.conditions.isEmpty()
}
