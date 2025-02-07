package actual.url.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Immutable
enum class Protocol(private val value: String) {
  Http("http"),
  Https("https"),
  ;

  @Stable
  override fun toString(): String = value

  companion object {
    fun fromString(value: String): Protocol = entries
      .firstOrNull { it.value == value }
      ?: error("No protocol matching $value")
  }
}
