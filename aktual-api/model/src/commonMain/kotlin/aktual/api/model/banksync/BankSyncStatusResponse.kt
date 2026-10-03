package aktual.api.model.banksync

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// packages/loot-core/src/types/models/bank-sync.ts BankSyncProviderStatus
sealed interface BankSyncStatusResponse {
  @Serializable
  data class Success(
    @SerialName("configured") val configured: Boolean = false,
    @SerialName("source") val source: BankSyncCredentialSource? = null,
  ) : BankSyncStatusResponse

  data class Rejected(val reason: String?, val details: String?) : BankSyncStatusResponse
}

@Serializable
enum class BankSyncCredentialSource {
  @SerialName("per-budget-file") PerBudgetFile,
  @SerialName("global") Global,
}
