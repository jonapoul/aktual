package actual.api.model.account

import actual.account.model.Password
import dev.zacsweers.redacted.annotations.Redacted
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChangePasswordRequest(
  @Redacted @SerialName("password") val password: Password,
)
