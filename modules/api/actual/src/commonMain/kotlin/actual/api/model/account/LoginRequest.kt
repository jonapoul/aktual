package actual.api.model.account

import actual.account.model.LoginMethod
import actual.account.model.Password
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.afanasev.sekret.Secret

@Serializable
data class LoginRequest(
  @SerialName("loginMethod") val loginMethod: LoginMethod,
  @Secret @SerialName("password") val password: Password,
)
