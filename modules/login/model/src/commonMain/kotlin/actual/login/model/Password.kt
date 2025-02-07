package actual.login.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Immutable
@JvmInline
value class Password(private val value: String) {
  @Stable
  override fun toString(): String = value

  companion object {
    val Empty = Password(value = "")
    val Dummy = Password(value = "abcd-1234")
  }
}
