package actual.api.model.sync

import actual.api.model.Response
import actual.api.model.ResponseStatus
import actual.api.model.internal.ListUserFileSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable(ListUserFileSerializer::class)
sealed interface ListUserFilesResponse : Response {
  @Serializable
  data class Ok(
    @SerialName("data") val data: List<Item>,
    @SerialName("status") override val status: ResponseStatus = ResponseStatus.Ok,
  ) : ListUserFilesResponse

  @Serializable
  data class Error(
    @SerialName("reason") val reason: String,
    @SerialName("status") override val status: ResponseStatus = ResponseStatus.Error,
  ) : ListUserFilesResponse

  @Serializable
  data class Item(
    @SerialName("deleted") val deleted: Int,
    @SerialName("fileId") val fileId: String,
    @SerialName("groupId") val groupId: String,
    @SerialName("name") val name: String,
    @SerialName("encryptKeyId") val encryptKeyId: String?,
    @SerialName("owner") val owner: String?,
    @SerialName("usersWithAccess") val usersWithAccess: List<UserWithAccess>,
  )

  @Serializable
  data class UserWithAccess(
    @SerialName("userId") val userId: String,
    @SerialName("userName") val userName: String,
    @SerialName("displayName") val displayName: String,
    @SerialName("owner") val isOwner: Boolean,
  )
}
