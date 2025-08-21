package actual.core.model

import dev.zacsweers.redacted.annotations.Redacted
import kotlinx.serialization.Serializable
import java.util.stream.IntStream

@Redacted
@JvmInline
@Serializable
value class Password(val value: String) : CharSequence by value {
  override fun chars(): IntStream = value.chars()
  override fun codePoints(): IntStream = value.codePoints()

  fun interface Provider {
    fun default(): Password
  }

  companion object {
    val Empty = Password(value = "")
    val Dummy = Password(value = "abcd-1234")
  }
}
