package actual.api.model.account

import actual.account.model.LoginMethod
import actual.account.model.PossibleRole
import dev.drewhamilton.poko.Poko
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface ValidateResponse {
  @Serializable
  @Poko class Success(
    @SerialName("data") val data: Data,
  ) : ValidateResponse

  @Serializable
  @Poko class Failure(
    @SerialName("reason") val reason: FailureReason,
  ) : ValidateResponse

  @Serializable
  @Poko class Data(
    @SerialName("userName") val userName: String,
    @SerialName("permission") val permission: PossibleRole,
    @SerialName("userId") val userId: String,
    @SerialName("displayName") val displayName: String,
    @SerialName("loginMethod") val loginMethod: LoginMethod,
  )
}
