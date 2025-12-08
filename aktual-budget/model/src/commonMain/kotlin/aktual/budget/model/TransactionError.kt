package aktual.budget.model

import alakazam.kotlin.serialization.SerializableByString
import alakazam.kotlin.serialization.enumStringSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionError(
  @SerialName("difference") val difference: Int,
  @SerialName("type") val type: Type = Type.SplitTransactionError,
  @SerialName("version") val version: Int,
) {
  @Serializable(Type.Serializer::class)
  enum class Type(override val value: String) : SerializableByString {
    SplitTransactionError("SplitTransactionError"),
    ;

    internal object Serializer : KSerializer<Type> by enumStringSerializer()
  }
}
