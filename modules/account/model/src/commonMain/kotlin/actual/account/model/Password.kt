package actual.account.model

import dev.zacsweers.redacted.annotations.Redacted
import kotlinx.serialization.Serializable

@Redacted
@JvmInline
@Serializable
value class Password(val value: String) {
  fun interface Provider {
    fun default(): Password
  }

  companion object {
    val Empty = Password(value = "")
    val Dummy = Password(value = "abcd-1234")
  }
}
