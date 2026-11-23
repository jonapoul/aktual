package aktual.budget.list.vm

import aktual.budget.model.BudgetId
import androidx.compose.runtime.Immutable

// Ported from packages/loot-core/src/server/util/budget-name.ts

const val MAX_BUDGET_NAME_LENGTH = 100

@Immutable
sealed interface BudgetNameError {
  data object Blank : BudgetNameError

  data object TooLong : BudgetNameError

  @JvmInline value class Taken(val name: String) : BudgetNameError
}

fun validateBudgetName(name: String, existing: Collection<String>): BudgetNameError? {
  val trimmed = name.trim()
  val matching = existing.firstOrNull { it == trimmed }
  return when {
    matching != null -> BudgetNameError.Taken(matching)
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
