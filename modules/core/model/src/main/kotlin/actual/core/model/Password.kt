package actual.core.model

import androidx.compose.runtime.Immutable

@Immutable
@JvmInline
value class Password(private val value: String) {
  override fun toString(): String = value

  companion object {
    val Empty = Password(value = "")
  }
}
