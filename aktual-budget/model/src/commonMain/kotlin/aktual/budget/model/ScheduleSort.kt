package aktual.budget.model

import alakazam.kotlin.SerializableByString
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.toImmutableList

@Immutable
data class ScheduleSort(val field: Field, val direction: SortDirection) {
  companion object {
    val Default = ScheduleSort(Field.Default, SortDirection.Default)
  }

  enum class Field(override val value: String) : SerializableByString {
    NextDate("nextDate"),
    Name("name"),
    Payee("payee"),
    Account("account"),
    Amount("amount"),
    Status("status");

    companion object {
      val Default = NextDate
      val Entries = entries.toImmutableList()
    }
  }
}
