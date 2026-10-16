package aktual.budget.model

import alakazam.kotlin.SerializableByString
import kotlinx.collections.immutable.toImmutableList

enum class SortDirection(override val value: String) : SerializableByString {
  Ascending("ascending"),
  Descending("descending");

  fun <T> apply(comparator: Comparator<T>): Comparator<T> =
    when (this) {
      Ascending -> comparator
      Descending -> comparator.reversed()
    }

  companion object {
    val Default = Ascending
    val Entries = entries.toImmutableList()
  }
}
