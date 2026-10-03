package aktual.api.model.banksync

import kotlinx.serialization.DeserializationStrategy
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
    @SerialName("balances") val balances: List<BankSyncBalance> = [],
    // The account's current balance in cents, despite the name
    @SerialName("startingBalance") val startingBalance: Long? = null,
  ) : BankSyncTransactionsResponse {
    @Serializable
    data class Transactions(
      @SerialName("all") val all: List<BankSyncTransaction> = [],
      @SerialName("booked") val booked: List<BankSyncTransaction> = [],
      @SerialName("pending") val pending: List<BankSyncTransaction> = [],
    )
  }

  /** The server reached the provider, but the provider refused or failed the request. */
  @Serializable
  data class ProviderError(
    @SerialName("error_type") val errorType: String,
    @SerialName("error_code") val errorCode: String,
    @SerialName("reason") val reason: String? = null,
  ) : BankSyncTransactionsResponse

  /** The server refused the request itself, e.g. the provider isn't configured. */
  data class Rejected(val reason: String?, val details: String?) : BankSyncTransactionsResponse
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
  override fun selectDeserializer(
    element: JsonElement
  ): DeserializationStrategy<BankSyncTransactionsResponse> =
    if ("error_code" in element.jsonObject) {
      BankSyncTransactionsResponse.ProviderError.serializer()
    } else {
      BankSyncTransactionsResponse.Success.serializer()
    }
}
