package actual.api.model.account

import actual.account.model.Password
import actual.api.model.internal.PasswordSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
  @SerialName("password")
  @Serializable(with = PasswordSerializer::class)
  val password: Password,
)
