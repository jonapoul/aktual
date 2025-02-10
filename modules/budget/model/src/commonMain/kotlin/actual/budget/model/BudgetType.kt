package actual.budget.model

/**
 * packages/loot-core/src/server/prefs.ts#BUDGET_TYPES
 */
enum class BudgetType(private val value: String) {
  Rollover("rollover"),
  Report("report"),
  ;

  override fun toString(): String = value

  companion object {
    fun fromString(string: String): BudgetType = entries
      .firstOrNull { it.value == string }
      ?: error("No BudgetType matching $string")
  }
}
