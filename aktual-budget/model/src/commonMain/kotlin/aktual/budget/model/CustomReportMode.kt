package aktual.budget.model

import alakazam.kotlin.SerializableByString

enum class CustomReportMode(override val value: String) : SerializableByString {
  Total(value = "total"),
  Time(value = "time");

  override fun toString(): String = value
}
