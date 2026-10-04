package aktual.api.model.banksync

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

// packages/loot-core/src/types/models/bank-sync.ts BankSyncResponse
@Serializable(BankSyncTransactionsResponseSerializer::class)
sealed interface BankSyncTransactionsResponse {
  @Serializable
  data class Success(
    @SerialName("transactions") val transactions: Transactions,
    @SerialName("balances") val balances: List<BankSyncBalance> = emptyList(),
    // The account's current balance in cents, despite the name
    @SerialName("startingBalance") val startingBalance: Long? = null,
  ) : BankSyncTransactionsResponse {
    @Serializable
    data class Transactions(
      @SerialName("all") val all: List<BankSyncTransaction> = emptyList(),
      @SerialName("booked") val booked: List<BankSyncTransaction> = emptyList(),
      @SerialName("pending") val pending: List<BankSyncTransaction> = emptyList(),
    )
  }

  sealed interface Failure : BankSyncTransactionsResponse

  @Serializable
  data class ProviderError(
    @SerialName("error_type") val errorType: String,
    @SerialName("error_code") val errorCode: String,
    @SerialName("reason") val reason: String? = null,
  ) : Failure {
    companion object {
      // Upstream's codes for failures that don't come from the provider
      const val ACCOUNT_MISSING = "ACCOUNT_MISSING"
      const val NO_DATA = "NO_DATA"
      const val TIMED_OUT = "TIMED_OUT"
    }
  }

  data class Rejected(val reason: String?, val details: String?) : Failure
}

@Serializable
data class BankSyncBalance(
  @SerialName("balanceAmount") val balanceAmount: BankSyncAmount,
  @SerialName("balanceType") val balanceType: String,
  @SerialName("referenceDate") val referenceDate: String? = null,
)

@Serializable
data class BankSyncAmount(
  // Usually a decimal string like "-12.34", but Pluggy.ai sends a number
  @SerialName("amount") @Serializable(LenientStringSerializer::class) val amount: String,
  @SerialName("currency") val currency: String? = null,
)

internal object BankSyncTransactionsResponseSerializer :
  JsonContentPolymorphicSerializer<BankSyncTransactionsResponse>(
    BankSyncTransactionsResponse::class
  ) {
  override fun selectDeserializer(element: JsonElement) =
    if ("error_code" in element.jsonObject) {
      BankSyncTransactionsResponse.ProviderError.serializer()
    } else {
      BankSyncTransactionsResponse.Success.serializer()
    }
}
