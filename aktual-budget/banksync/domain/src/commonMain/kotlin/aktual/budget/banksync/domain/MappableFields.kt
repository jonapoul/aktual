package aktual.budget.banksync.domain

import aktual.budget.db.dao.BankSyncDao
import aktual.budget.model.AccountId
import dev.zacsweers.metro.Inject
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

data class FieldExample(val field: String, val example: String)

@Inject
class MappableFieldsLoader(private val dao: BankSyncDao) {
  suspend fun load(
    account: AccountId,
    direction: TransactionDirection,
  ): Map<MappedField, List<FieldExample>>? {
    val data = dao.exampleData(account, deposit = direction == Deposit) ?: return null
    val transaction =
      try {
        Json.parseToJsonElement(data) as? JsonObject
      } catch (_: SerializationException) {
        null
      }
    return transaction?.let(::mappableFields)
  }
}

/**
 * getFields() in packages/desktop-client/src/components/banksync/EditSyncAccount.tsx: of the fields
 * each [MappedField] can be mapped from, the ones [transaction] has, with their values. Dotted
 * fields are looked up through nested objects, though the import only reads top-level ones.
 */
fun mappableFields(transaction: JsonObject): Map<MappedField, List<FieldExample>> =
  MAPPABLE_FIELDS.mapValues { (_, fields) ->
    fields.mapNotNull { field ->
      transaction.byPath(field)?.let { value -> FieldExample(field, value.example()) }
    }
  }

private fun JsonObject.byPath(path: String): JsonElement? =
  path.split('.').fold(this as JsonElement?) { current, key -> (current as? JsonObject)?.get(key) }

// As JavaScript's String(value)
private fun JsonElement.example(): String =
  when (this) {
    JsonNull -> "null"
    is JsonPrimitive -> content
    is JsonArray -> joinToString(",") { if (it is JsonNull) "" else it.example() }
    is JsonObject -> "[object Object]"
  }

private val MAPPABLE_FIELDS: Map<MappedField, List<String>> =
  mapOf(
    MappedField.Date to
      listOf(
        "date",
        "bookingDate",
        "valueDate",
        "postedDate",
        "transactedDate",
        "booking_date",
        "value_date",
        "transaction_date",
        "originalDate",
      ),
    MappedField.Payee to
      listOf(
        "payeeName",
        "creditorName",
        "debtorName",
        "remittanceInformationUnstructured",
        "remittanceInformationUnstructuredArrayString",
        "remittanceInformationStructured",
        "remittanceInformationStructuredArrayString",
        "additionalInformation",
        "paymentData.payer.accountNumber",
        "paymentData.payer.documentNumber.value",
        "paymentData.payer.name",
        "paymentData.receiver.accountNumber",
        "paymentData.receiver.documentNumber.value",
        "paymentData.receiver.name",
        "merchant.name",
        "merchant.businessName",
        "merchant.cnpj",
        "creditor.name",
        "debtor.name",
        "account_servicer.name",
        "meta.other_account",
      ),
    MappedField.Notes to
      listOf(
        "notes",
        "remittanceInformationUnstructured",
        "remittanceInformationUnstructuredArrayString",
        "remittanceInformationStructured",
        "remittanceInformationStructuredArrayString",
        "additionalInformation",
        "category",
        "paymentData.payer.accountNumber",
        "paymentData.payer.documentNumber.value",
        "paymentData.payer.name",
        "paymentData.receiver.accountNumber",
        "paymentData.receiver.documentNumber.value",
        "paymentData.receiver.name",
        "merchant.name",
        "merchant.businessName",
        "merchant.cnpj",
        "entry_reference",
        "transaction_id",
        "meta.particulars",
        "meta.code",
        "meta.reference",
        "meta.other_account",
        "meta.card_suffix",
      ),
  )
