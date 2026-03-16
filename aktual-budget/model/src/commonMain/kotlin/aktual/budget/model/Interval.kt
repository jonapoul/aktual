package aktual.budget.model

import alakazam.kotlin.SerializableByString

enum class Interval(override val value: String) : SerializableByString {
  Daily("Daily"),
  Weekly("Weekly"),
  Monthly("Monthly"),
  Yearly("Yearly"),
}
