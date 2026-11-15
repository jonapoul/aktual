package aktual.budget.banksync.domain

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

// mappingsFromString(), which throws on anything that isn't a JSON object
fun SyncMappings.Companion.parse(string: String): SyncMappings {
  val json =
    try {
      Json.parseToJsonElement(string) as? JsonObject
    } catch (e: SerializationException) {
      throw IllegalArgumentException("Failed to parse mapping", e)
    } ?: throw IllegalArgumentException("Failed to parse mapping: Invalid mapping format")
  return SyncMappings(
    payment = json.mapping(TransactionDirection.Payment.key) ?: Default.payment,
    deposit = json.mapping(TransactionDirection.Deposit.key) ?: Default.deposit,
  )
}

private fun JsonObject.mapping(key: String): FieldMapping? {
  val fields = this[key] as? JsonObject ?: return null
  fun field(name: String) = (fields[name] as? JsonPrimitive)?.contentOrNull
  return FieldMapping(
    date = field(MappedField.Date.key),
    payee = field(MappedField.Payee.key),
    notes = field(MappedField.Notes.key),
  )
}

fun SyncMappings.encode(): String = buildJsonObject {
  TransactionDirection.entries.forEach { direction ->
    putJsonObject(direction.key) {
      MappedField.entries.forEach { field ->
        this@encode[direction][field]?.let { put(field.key, it) }
      }
    }
  }
}
  .toString()

private val MappedField.key: String
  get() =
    when (this) {
      Date -> "date"
      Payee -> "payee"
      Notes -> "notes"
    }

private val TransactionDirection.key: String
  get() =
    when (this) {
      Payment -> "payment"
      Deposit -> "deposit"
    }
