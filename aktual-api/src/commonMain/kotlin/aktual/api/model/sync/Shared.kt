package aktual.api.model.sync

import aktual.budget.encryption.Meta
import aktual.budget.model.BudgetId
import aktual.core.model.KeyId
import aktual.core.model.SerializableByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserFile(
  @SerialName("deleted") val deleted: Int,
  @SerialName("fileId") val fileId: BudgetId,
  @SerialName("groupId") val groupId: String,
  @SerialName("name") val name: String,
  @SerialName("encryptKeyId") val encryptKeyId: KeyId? = null,
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
  @SerialName("keyId") override val keyId: KeyId?,
  @SerialName("algorithm") override val algorithm: String,
  @SerialName("iv") override val iv: SerializableByteString,
  @SerialName("authTag") override val authTag: SerializableByteString,
) : Meta
