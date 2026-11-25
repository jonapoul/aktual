package aktual.api.model.apikeys

import aktual.budget.model.BudgetId
import aktual.core.model.Token
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class ApiKeyId(val value: String) {
  override fun toString(): String = value
}

// A value class rather than an enum, so keys with permissions from newer servers still decode
@JvmInline
@Serializable
value class ApiKeyPermission(val value: String) {
  override fun toString(): String = value

  companion object {
    // Bank sync with the server's shared provider credentials, not just the budget's own
    val BankSyncGlobal = ApiKeyPermission("bank-sync-global")
  }
}

@Serializable
data class ApiKey(
  @SerialName("id") val id: ApiKeyId,
  @SerialName("name") val name: String,
  @SerialName("fileIds") val budgets: List<BudgetId>,
  @SerialName("createdAt") val createdAtSeconds: Long,
  @SerialName("permissions") val permissions: List<ApiKeyPermission> = emptyList(),
)

// The server only returns the key itself once, when it's created
@Serializable
data class CreatedApiKey(
  @SerialName("id") val id: ApiKeyId,
  @SerialName("name") val name: String,
  @SerialName("fileIds") val budgets: List<BudgetId>,
  @SerialName("createdAt") val createdAtSeconds: Long,
  @SerialName("key") val key: Token,
  @SerialName("permissions") val permissions: List<ApiKeyPermission> = emptyList(),
)

@Serializable
data class CreateApiKeyRequest(
  @SerialName("name") val name: String,
  @SerialName("fileIds") val budgets: List<BudgetId>,
  // Null when empty, so older servers never see the field
  @SerialName("permissions") val permissions: List<ApiKeyPermission>? = null,
)
