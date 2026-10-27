package aktual.budget.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

/** Keys are equal by [name] alone, so separately-created keys find the same value. */
@Suppress("UseDataClass") // a data class would compare the lambdas too
class DbMetadataKey<T : Any>(
  val name: String,
  val decode: (JsonElement) -> T,
  val encode: (T) -> JsonElement,
) {
  override fun equals(other: Any?) = other is DbMetadataKey<*> && other.name == name

  override fun hashCode() = name.hashCode()

  override fun toString() = "DbMetadataKey($name)"

  companion object {
    val BudgetName: DbMetadataKey<String> = stringKey("budgetName")
    val BudgetCollapsed: DbMetadataKey<List<String>> = listKey("budget.collapsed")
    val BudgetShowHiddenCategories: DbMetadataKey<Boolean> = boolKey("budget.showHiddenCategories")
    val BudgetMonthCount: DbMetadataKey<Int> = intKey("budget.monthCount")
    val MobileShowSpentColumn: DbMetadataKey<Boolean> = boolKey("mobile.showSpentColumn")
    val HomeCardOrder: DbMetadataKey<List<String>> = listKey("home.cardOrder")
    val HomeReports: DbMetadataKey<List<String>> = listKey("home.reports")
    val CloudFileId: DbMetadataKey<BudgetId> = typedKey("cloudFileId", ::BudgetId)
    val GroupId: DbMetadataKey<String> = stringKey("groupId")
    val Id: DbMetadataKey<String> = stringKey("id")
    val LastScheduleRun: DbMetadataKey<LocalDate> = typedKey("lastScheduleRun", LocalDate::parse)
    val LastSyncedTimestamp: DbMetadataKey<Timestamp> =
      typedKey("lastSyncedTimestamp", Timestamp::parse)
    val LastUploaded: DbMetadataKey<LocalDate> = typedKey("lastUploaded", LocalDate::parse)
    val ResetClock: DbMetadataKey<Boolean> = boolKey("resetClock")
    val UserId: DbMetadataKey<String> = stringKey("userId")
    val EncryptKeyId: DbMetadataKey<String> = stringKey("encryptKeyId")
    val TransactionDensity: DbMetadataKey<TransactionsDensity> = enumKey("transactionDensity")

    fun boolKey(name: String): DbMetadataKey<Boolean> =
      DbMetadataKey(name, decode = { it.jsonPrimitive.boolean }, encode = { JsonPrimitive(it) })

    fun intKey(name: String): DbMetadataKey<Int> =
      DbMetadataKey(name, decode = { it.jsonPrimitive.int }, encode = { JsonPrimitive(it) })

    fun stringKey(name: String): DbMetadataKey<String> =
      DbMetadataKey(name, decode = { it.jsonPrimitive.content }, encode = { JsonPrimitive(it) })

    fun listKey(name: String): DbMetadataKey<List<String>> =
      DbMetadataKey(
        name,
        decode = { it.jsonArray.map { element -> element.jsonPrimitive.content } },
        encode = { JsonArray(it.map(::JsonPrimitive)) },
      )

    /** Written with [toString], read with [fromString]. */
    fun <T : Any> typedKey(name: String, fromString: (String) -> T): DbMetadataKey<T> =
      DbMetadataKey(
        name,
        decode = { fromString(it.jsonPrimitive.content) },
        encode = { JsonPrimitive(it.toString()) },
      )

    /** Written and read by [Enum.name]. */
    inline fun <reified E : Enum<E>> enumKey(name: String): DbMetadataKey<E> =
      DbMetadataKey(
        name,
        decode = { element -> enumValues<E>().first { it.name == element.jsonPrimitive.content } },
        encode = { JsonPrimitive(it.name) },
      )
  }
}
