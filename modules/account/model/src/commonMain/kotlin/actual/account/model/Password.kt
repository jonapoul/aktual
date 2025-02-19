package actual.account.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
@JvmInline
value class Password(private val value: String) : CharSequence by value {
  @Stable
  override fun toString(): String = value

  companion object {
    val Empty = Password(value = "")
    val Dummy = Password(value = "abcd-1234")
  }
}
