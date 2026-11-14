package aktual.budget.db

import aktual.budget.model.Condition
import aktual.budget.model.RuleAction
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.contentOrNull

/**
 * Serializers for the `conditions` and `actions` columns of the `rules` table, and for sync
 * messages that write them.
 *
 * Upstream stores rule fields under the `transactions` table's column names where they differ from
 * the public ones: `payee` is saved as `description`, `account` as `acct` and `imported_payee` as
 * `imported_description`. It renames them on the way in and out of the database with
 * `toInternalField` / `fromInternalField` in
 * packages/loot-core/src/server/transactions/transaction-rules.ts, and its SQL queries rely on the
 * internal names. These serializers do the same, so the app only ever sees the public names.
 *
 * Saved filters and custom reports store conditions with the public names, so they don't use these.
 */
object RuleJson {
  val conditions: KSerializer<List<Condition>> =
    InternalFieldSerializer(ListSerializer(Condition.serializer()))

  val actions: KSerializer<List<RuleAction>> =
    InternalFieldSerializer(ListSerializer(RuleAction.serializer()))

  // schemaConfig.views.transactions.fields in packages/loot-core/src/server/aql/schema/index.ts,
  // limited to the fields rules can use
  private val PUBLIC_TO_INTERNAL =
    mapOf(
      "payee" to "description",
      "account" to "acct",
      "imported_payee" to "imported_description",
    )

  private val INTERNAL_TO_PUBLIC = PUBLIC_TO_INTERNAL.entries.associate { (k, v) -> v to k }

  private class InternalFieldSerializer<T : Any>(serializer: KSerializer<List<T>>) :
    JsonTransformingSerializer<List<T>>(serializer) {
    override fun transformDeserialize(element: JsonElement): JsonElement =
      element.renameFields(INTERNAL_TO_PUBLIC)

    override fun transformSerialize(element: JsonElement): JsonElement =
      element.renameFields(PUBLIC_TO_INTERNAL)
  }

  private fun JsonElement.renameFields(names: Map<String, String>): JsonElement {
    if (this !is JsonArray) return this
    return JsonArray(
      map { item ->
        if (item !is JsonObject) return@map item
        val field = (item[FIELD] as? JsonPrimitive)?.contentOrNull
        val renamed = names[field] ?: return@map item
        JsonObject(item + (FIELD to JsonPrimitive(renamed)))
      },
    )
  }

  private const val FIELD = "field"
}
