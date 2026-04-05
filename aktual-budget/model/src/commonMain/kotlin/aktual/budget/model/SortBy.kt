package aktual.budget.model

import alakazam.kotlin.SerializableByString

enum class SortBy(override val value: String) : SerializableByString {
  Asc(value = "asc"),
  Budget(value = "budget"),
  Desc(value = "desc"),
  Name(value = "name");

  override fun toString(): String = value
}
