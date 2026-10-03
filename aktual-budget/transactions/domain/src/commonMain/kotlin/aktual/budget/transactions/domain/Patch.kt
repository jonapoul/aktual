package aktual.budget.transactions.domain

/**
 * One field of a partial update to a nullable column. [Keep] leaves the stored value alone, [To]
 * overwrites it, with null if that's what it holds.
 */
sealed interface Patch<out T> {
  data object Keep : Patch<Nothing>

  data class To<out T>(val value: T) : Patch<T>
}
