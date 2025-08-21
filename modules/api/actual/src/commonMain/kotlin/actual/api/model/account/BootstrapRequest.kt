package actual.api.model.account

import actual.core.model.Password
import dev.zacsweers.redacted.annotations.Redacted
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BootstrapRequest(
  @Redacted @SerialName("password") val password: Password,
)
