package actual.budget.model

import androidx.compose.runtime.Immutable

@Immutable
data class DateFormat(
  val value: String,
  val label: String,
) {
  companion object : Set<DateFormat> by Entries {
    val Default = Entries.first()

    fun from(value: String?): DateFormat? = Entries.firstOrNull { it.value == value }
  }
}

private val Entries = setOf(
  DateFormat(value = "MM/dd/yyyy", label = "MM/DD/YYYY"),
  DateFormat(value = "dd/MM/yyyy", label = "MM/DD/YYYY"),
  DateFormat(value = "yyyy-MM-dd", label = "YYYY-MM-DD"),
  DateFormat(value = "MM.dd.yyyy", label = "MM.DD.YYYY"),
  DateFormat(value = "dd.MM.yyyy", label = "DD.MM.YYYY"),
)
