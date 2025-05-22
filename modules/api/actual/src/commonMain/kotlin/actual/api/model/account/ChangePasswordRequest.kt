package actual.api.model.account

import actual.account.model.LoginToken
import actual.account.model.Password
import dev.drewhamilton.poko.Poko
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Poko class ChangePasswordRequest(
  @SerialName("token") val token: LoginToken,
  @SerialName("password") val password: Password,
)
