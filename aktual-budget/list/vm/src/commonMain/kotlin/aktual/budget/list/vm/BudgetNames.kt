package aktual.budget.list.vm

import aktual.budget.model.BudgetId
import androidx.compose.runtime.Immutable

// Ported from packages/loot-core/src/server/util/budget-name.ts

const val DEFAULT_BUDGET_NAME = "My Finances"
const val MAX_BUDGET_NAME_LENGTH = 100

@Immutable
sealed interface BudgetNameError {
  data object Blank : BudgetNameError

  data object TooLong : BudgetNameError

  @JvmInline value class Taken(val suggestion: String) : BudgetNameError
}

/** Appends an index to [initialName] until it doesn't clash with [existing]. */
fun uniqueBudgetName(
  existing: Collection<String>,
  initialName: String = DEFAULT_BUDGET_NAME,
): String {
  var name = initialName
  var index = 1
  while (name in existing) {
    name = "$initialName $index"
    index++
  }
  return name
}

/** Returns null if [name] can be used for a new budget. */
fun validateBudgetName(name: String, existing: Collection<String>): BudgetNameError? {
  val trimmed = name.trim()
  val unique = uniqueBudgetName(existing, trimmed)
  return when {
    unique != trimmed -> BudgetNameError.Taken(unique)
    trimmed.length > MAX_BUDGET_NAME_LENGTH -> TooLong
    trimmed.isEmpty() -> Blank
    else -> null
  }
}

// idFromBudgetName in budget-name.ts
internal fun budgetIdFromName(name: String, uuid: String): BudgetId =
  BudgetId(name.replace(NON_ALPHANUMERIC, "-") + "-" + uuid.take(ID_SUFFIX_LENGTH))

private val NON_ALPHANUMERIC = Regex("[^A-Za-z0-9]")
private const val ID_SUFFIX_LENGTH = 7
