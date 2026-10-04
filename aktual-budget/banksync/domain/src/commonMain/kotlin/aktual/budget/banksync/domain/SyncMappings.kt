package aktual.budget.banksync.domain

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * Which fields of a downloaded transaction fill in its date, payee and notes. Each holds the name
 * of a top-level field of the provider's transaction, null if the mapping doesn't name one.
 */
data class FieldMapping(val date: String?, val payee: String?, val notes: String?)

/**
 * The field mappings of packages/loot-core/src/server/util/custom-sync-mapping.ts, for payments
 * (amounts of zero or less) and deposits. Upstream fails on a direction that's missing from custom
 * mappings, whereas this falls back to the default.
 */
data class SyncMappings(val payment: FieldMapping, val deposit: FieldMapping) {
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
        payment = json.mapping("payment") ?: DefaultMapping,
        deposit = json.mapping("deposit") ?: DefaultMapping,
      )
    }

    private fun JsonObject.mapping(key: String): FieldMapping? {
      val fields = this[key] as? JsonObject ?: return null
      fun field(name: String) = (fields[name] as? JsonPrimitive)?.contentOrNull
      return FieldMapping(date = field("date"), payee = field("payee"), notes = field("notes"))
    }
  }
}
