package aktual.api.model.banksync

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * A single transaction as normalised by the sync server. Each provider sends a different set of
 * fields, and an account's field mappings can pick any of them as the date, payee or notes, so the
 * raw JSON is kept as-is. It's also what gets stored in `transactions.raw_synced_data`.
 *
 * See packages/loot-core/src/types/models/gocardless.ts GoCardlessTransaction
 */
@Serializable(BankSyncTransactionSerializer::class)
@JvmInline
value class BankSyncTransaction(val json: JsonObject) {
  val transactionId: String?
    get() = this["transactionId"]

  val internalTransactionId: String?
    get() = this["internalTransactionId"]

  val isBooked: Boolean
    get() = json["booked"]?.jsonPrimitive?.booleanOrNull == true

  val date: String?
    get() = this["date"]

  val payeeName: String?
    get() = this["payeeName"]

  val amount: String?
    get() = json["transactionAmount"]?.jsonObject?.get("amount")?.jsonPrimitive?.contentOrNull

  /** Reads a top-level field as a string, for field mappings to look up by name. */
  operator fun get(key: String): String? = (json[key] as? JsonPrimitive)?.contentOrNull
}

internal object BankSyncTransactionSerializer : KSerializer<BankSyncTransaction> {
  private val delegate = JsonObject.serializer()
  override val descriptor = delegate.descriptor

  override fun deserialize(decoder: Decoder) = BankSyncTransaction(delegate.deserialize(decoder))

  override fun serialize(encoder: Encoder, value: BankSyncTransaction) =
    delegate.serialize(encoder, value.json)
}
