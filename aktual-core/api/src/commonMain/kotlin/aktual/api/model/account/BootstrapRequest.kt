package aktual.api.model.account

import aktual.core.model.Password
import dev.zacsweers.redacted.annotations.Redacted
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class BootstrapRequest(@Redacted @SerialName("password") val password: Password)
