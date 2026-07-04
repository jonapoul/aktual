package aktual.budget.model

import alakazam.kotlin.SerializableByString
import kotlinx.collections.immutable.toImmutableList

data class TagSort(val field: Field, val direction: Direction) {
  companion object {
    val Default = TagSort(Field.Default, Direction.Default)
  }

  enum class Field(override val value: String) : SerializableByString {
    Name("name"),
    Usage("usage");

    companion object {
      val Default = Name
      val Entries = entries.toImmutableList()
    }
  }

  enum class Direction(override val value: String) : SerializableByString {
    Ascending("ascending"),
    Descending("descending");

    companion object {
      val Default = Ascending
      val Entries = entries.toImmutableList()
    }
  }
}
