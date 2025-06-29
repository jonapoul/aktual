package actual.api.model.account

import actual.account.model.LoginMethod
import actual.account.model.Password
import dev.zacsweers.redacted.annotations.Redacted
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
  @SerialName("loginMethod") val loginMethod: LoginMethod,
  @Redacted @SerialName("password") val password: Password,
)
