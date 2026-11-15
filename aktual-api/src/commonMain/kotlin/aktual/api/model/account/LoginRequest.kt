package aktual.api.model.account

import aktual.core.model.LoginMethod
import aktual.core.model.Password as PasswordModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface LoginRequest {
  @Serializable
  data class Header(@SerialName("loginMethod") val loginMethod: LoginMethod = LoginMethod.Header) :
    LoginRequest

  @Serializable
  data class OpenId(
    @SerialName("password") val password: PasswordModel,
    @SerialName("returnUrl") val returnUrl: String,
    @SerialName("loginMethod") val loginMethod: LoginMethod = LoginMethod.OpenId,
  ) : LoginRequest

  @Serializable
  data class Password(
    @SerialName("password") val password: PasswordModel,
    @SerialName("loginMethod") val loginMethod: LoginMethod = LoginMethod.Password,
  ) : LoginRequest
}
