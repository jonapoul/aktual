package aktual.core.model

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class Token(val value: String) {
  // Budget API keys, as opposed to a session from logging in with a password or OpenID
  val isApiKey: Boolean
    get() = value.startsWith(API_KEY_PREFIX)

  override fun toString(): String = value

  private companion object {
    const val API_KEY_PREFIX = "actual_"
  }
}
