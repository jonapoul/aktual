package aktual.budget.model

import alakazam.kotlin.SerializableByString

enum class GroupBy(override val value: String) : SerializableByString {
  Account(value = "Account"),
  Category(value = "Category"),
  Group(value = "Group"),
  Interval(value = "Interval"),
  Payee(value = "Payee");

  override fun toString(): String = value
}
