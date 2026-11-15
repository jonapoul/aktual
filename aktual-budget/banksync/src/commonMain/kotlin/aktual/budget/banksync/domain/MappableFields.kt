package aktual.budget.banksync.domain

import aktual.budget.model.AccountId

data class FieldExample(val field: String, val example: String)

interface MappableFieldsLoader {
  suspend fun load(
    account: AccountId,
    direction: TransactionDirection,
  ): Map<MappedField, List<FieldExample>>?
}
