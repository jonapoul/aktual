package aktual.api.model.sync

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadUserFileResponse(
  @SerialName("status") val status: String,
  @SerialName("groupId") val groupId: String,
)
