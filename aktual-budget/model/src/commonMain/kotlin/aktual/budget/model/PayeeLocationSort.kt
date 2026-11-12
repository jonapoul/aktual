package aktual.budget.model

import alakazam.kotlin.SerializableByString
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.toImmutableList

@Immutable
data class PayeeLocationSort(val field: Field, val direction: SortDirection) {
  companion object {
    val Default = PayeeLocationSort(Field.Default, SortDirection.Default)
  }

  enum class Field(override val value: String) : SerializableByString {
    PayeeName("payeeName"),
    DateSaved("dateSaved");

    companion object {
      val Default = PayeeName
      val Entries = entries.toImmutableList()
    }
  }
}
