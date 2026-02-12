package aktual.budget.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class CsvMappings(
  @SerialName("date") @Serializable(with = StringIntSerializer::class) val date: Int,
  @SerialName("amount") @Serializable(with = StringIntSerializer::class) val amount: Int,
  @SerialName("payee") @Serializable(with = StringIntSerializer::class) val payee: Int,
  @SerialName("notes") val notes: Int,
  @SerialName("inOut") val inOut: Int,
  @SerialName("category") @Serializable(with = StringIntSerializer::class) val category: Int,
)

private object StringIntSerializer : KSerializer<Int> {
  override val descriptor = PrimitiveSerialDescriptor(serialName = "Int", PrimitiveKind.STRING)

  override fun deserialize(decoder: Decoder): Int = decoder.decodeString().toInt()

  override fun serialize(encoder: Encoder, value: Int) = encoder.encodeString(value.toString())
}
