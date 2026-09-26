package aktual.budget.model

import fallback.serializer.Fallback
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionError(
  @SerialName("difference") val difference: Int,
  @SerialName("type") val type: Type = Type.SplitTransactionError,
  @SerialName("version") val version: Int,
) {
  @Serializable
  enum class Type {
    @SerialName("SplitTransactionError") SplitTransactionError,
    @Fallback Unknown,
  }
}
