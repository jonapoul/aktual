package actual.account.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import java.io.Serializable as JavaIoSerializable

@Immutable
@Serializable
@JvmInline
value class LoginToken(val value: String) : JavaIoSerializable {
  override fun toString(): String = value
}
