package actual.api.model.sync

import actual.budget.model.BudgetId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserFile(
  @SerialName("deleted") val deleted: Int,
  @SerialName("fileId") val fileId: BudgetId,
  @SerialName("groupId") val groupId: String,
  @SerialName("name") val name: String,
  @SerialName("encryptKeyId") val encryptKeyId: String? = null,
  @SerialName("owner") val owner: String? = null,
  @SerialName("encryptMeta") val encryptMeta: EncryptMeta? = null,
  @SerialName("usersWithAccess") val usersWithAccess: List<UserWithAccess> = emptyList(),
)

@Serializable
data class UserWithAccess(
  @SerialName("userId") val userId: String,
  @SerialName("userName") val userName: String,
  @SerialName("displayName") val displayName: String,
  @SerialName("owner") val isOwner: Boolean,
)

@Serializable
data class EncryptMeta(
  @SerialName("keyId") val keyId: String,
  @SerialName("algorithm") val algorithm: String,
  @SerialName("iv") val iv: String,
  @SerialName("authTag") val authTag: String,
)
