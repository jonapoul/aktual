package actual.api.model.account

import actual.account.model.Password
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BootstrapRequest(
  @SerialName("password") val password: Password,
)
