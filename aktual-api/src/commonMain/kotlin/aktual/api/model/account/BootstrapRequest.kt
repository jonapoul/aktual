package aktual.api.model.account

import aktual.core.model.Password
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class BootstrapRequest(@SerialName("password") val password: Password)
