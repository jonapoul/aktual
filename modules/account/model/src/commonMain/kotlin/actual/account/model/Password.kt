package actual.account.model

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class Password(val value: String) : CharSequence by value {
  override fun toString(): String = value

  fun interface Provider {
    fun default(): Password
  }

  companion object {
    val Empty = Password(value = "")
    val Dummy = Password(value = "abcd-1234")
  }
}
