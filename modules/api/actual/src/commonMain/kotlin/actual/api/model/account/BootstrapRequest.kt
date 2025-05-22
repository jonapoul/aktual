package actual.api.model.account

import actual.account.model.Password
import dev.drewhamilton.poko.Poko
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.afanasev.sekret.Secret

@Serializable
@Poko class BootstrapRequest(
  @Secret @SerialName("password") val password: Password,
)
