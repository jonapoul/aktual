package actual.budget.model

/**
 * packages/loot-core/src/shared/rules.ts#ALLOCATION_METHODS
 */
enum class AllocationMethod(private val value: String) {
  FixedAmount("fixed-amount"), // a fixed amount
  FixedPercent("fixed-percent"), // a fixed percent of the remainder
  Remainder("remainder"), // an equal portion of the remainder
  ;

  override fun toString(): String = value

  companion object {
    fun fromString(string: String): AllocationMethod = entries
      .firstOrNull { it.value == string }
      ?: error("No AllocationMethod matching $string")
  }
}
