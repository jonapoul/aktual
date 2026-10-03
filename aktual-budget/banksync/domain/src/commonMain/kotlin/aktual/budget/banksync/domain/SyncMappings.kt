package aktual.budget.banksync.domain

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/** A transaction's fields that can be filled in from a downloaded transaction's */
enum class MappedField(internal val key: String) {
  Date("date"),
  Payee("payee"),
  Notes("notes"),
}

/** Which mapping applies: payments are amounts of zero or less */
enum class TransactionDirection(internal val key: String) {
  Payment("payment"),
  Deposit("deposit"),
}

/**
 * Which fields of a downloaded transaction fill in its date, payee and notes. Each holds the name
 * of a top-level field of the provider's transaction, null if the mapping doesn't name one.
 */
data class FieldMapping(val date: String?, val payee: String?, val notes: String?) {
  operator fun get(field: MappedField): String? =
    when (field) {
      Date -> date
      Payee -> payee
      Notes -> notes
    }
}

fun FieldMapping.with(field: MappedField, value: String): FieldMapping =
  when (field) {
    Date -> copy(date = value)
    Payee -> copy(payee = value)
    Notes -> copy(notes = value)
  }

/**
 * The field mappings of packages/loot-core/src/server/util/custom-sync-mapping.ts, for payments
 * (amounts of zero or less) and deposits. Upstream fails on a direction that's missing from custom
 * mappings, whereas this falls back to the default.
 */
data class SyncMappings(val payment: FieldMapping, val deposit: FieldMapping) {
  operator fun get(direction: TransactionDirection): FieldMapping =
    when (direction) {
      Payment -> payment
      Deposit -> deposit
    }

  companion object {
    private val DefaultMapping = FieldMapping(date = "date", payee = "payeeName", notes = "notes")

    val Default = SyncMappings(payment = DefaultMapping, deposit = DefaultMapping)

    /** mappingsFromString(), which throws on anything that isn't a JSON object. */
    fun parse(string: String): SyncMappings {
      val json =
        try {
          Json.parseToJsonElement(string) as? JsonObject
        } catch (e: SerializationException) {
          throw IllegalArgumentException("Failed to parse mapping: ${e.message}", e)
        } ?: throw IllegalArgumentException("Failed to parse mapping: Invalid mapping format")
      return SyncMappings(
        payment = json.mapping(TransactionDirection.Payment.key) ?: DefaultMapping,
        deposit = json.mapping(TransactionDirection.Deposit.key) ?: DefaultMapping,
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
  }
}

fun SyncMappings.with(
  direction: TransactionDirection,
  field: MappedField,
  value: String,
): SyncMappings =
  when (direction) {
    Payment -> copy(payment = payment.with(field, value))
    Deposit -> copy(deposit = deposit.with(field, value))
  }

/** mappingsToString(), the JSON that [SyncMappings.parse] reads */
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
