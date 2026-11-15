package aktual.budget.banksync.domain

enum class MappedField {
  Date,
  Payee,
  Notes,
}

enum class TransactionDirection {
  Payment,
  Deposit,
}

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

data class SyncMappings(val payment: FieldMapping, val deposit: FieldMapping) {
  operator fun get(direction: TransactionDirection): FieldMapping =
    when (direction) {
      Payment -> payment
      Deposit -> deposit
    }

  companion object {
    private val DefaultMapping = FieldMapping(date = "date", payee = "payeeName", notes = "notes")

    val Default = SyncMappings(payment = DefaultMapping, deposit = DefaultMapping)
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
