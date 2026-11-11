package aktual.api.model.apikeys

import aktual.budget.model.BudgetId
import aktual.core.model.Token
import dev.zacsweers.redacted.annotations.Redacted
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class ApiKeyId(val value: String) {
  override fun toString(): String = value
}

@Serializable
data class ApiKey(
  @SerialName("id") val id: ApiKeyId,
  @SerialName("name") val name: String,
  @SerialName("fileIds") val budgets: List<BudgetId>,
  @SerialName("createdAt") val createdAtSeconds: Long,
)

// The server only returns the key itself once, when it's created
@Serializable
data class CreatedApiKey(
  @SerialName("id") val id: ApiKeyId,
  @SerialName("name") val name: String,
  @SerialName("fileIds") val budgets: List<BudgetId>,
  @SerialName("createdAt") val createdAtSeconds: Long,
  @Redacted @SerialName("key") val key: Token,
)

@Serializable
data class CreateApiKeyRequest(
  @SerialName("name") val name: String,
  @SerialName("fileIds") val budgets: List<BudgetId>,
)
