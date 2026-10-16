package aktual.budget.model

import alakazam.kotlin.SerializableByString
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.toImmutableList

@Immutable
data class TagSort(val field: Field, val direction: SortDirection) {
  companion object {
    val Default = TagSort(Field.Default, SortDirection.Default)
  }

  enum class Field(override val value: String) : SerializableByString {
    Name("name"),
    Usage("usage");

    companion object {
      val Default = Name
      val Entries = entries.toImmutableList()
    }
  }
}
