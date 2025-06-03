package actual.api.model.account

import actual.account.model.LoginToken
import actual.account.model.Password
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.afanasev.sekret.Secret

@Serializable
data class ChangePasswordRequest(
  @SerialName("token") val token: LoginToken,
  @Secret @SerialName("password") val password: Password,
)
