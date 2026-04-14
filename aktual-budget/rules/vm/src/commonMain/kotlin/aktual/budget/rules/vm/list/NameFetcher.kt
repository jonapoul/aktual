package aktual.budget.rules.vm.list

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.CategoryDao
import aktual.budget.db.dao.CategoryGroupDao
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.ScheduleDao
import aktual.budget.model.AccountId
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import aktual.budget.model.Field
import aktual.budget.model.PayeeId
import aktual.budget.model.ScheduleId
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray

@Immutable
interface NameFetcher {
  fun name(field: Field, id: String): Flow<String?>

  fun name(id: ScheduleId): Flow<String?>

  fun names(field: Field, ids: List<String>): Flow<JsonArray>
}

@Suppress("ElseCaseInsteadOfExhaustiveWhen")
internal class NameFetcherImpl(database: BudgetDatabase) : NameFetcher {
  private val accounts = AccountDao(database)
  private val categories = CategoryDao(database)
  private val categoryGroups = CategoryGroupDao(database)
  private val payees = PayeeDao(database)
  private val schedules = ScheduleDao(database)

  // Make sure this stays in sync with aktual.budget.model.FieldKt.FIELDS_WITH_IDS
  override fun name(field: Field, id: String): Flow<String?> = flow {
    emit(
      when (field) {
        Field.Acct,
        Field.Account -> accounts.name(AccountId(id))

        Field.Category -> categories.name(CategoryId(id))

        Field.CategoryGroup -> categoryGroups.name(CategoryGroupId(id))

        Field.Description,
        Field.Payee -> payees.name(PayeeId(id))

        else -> "NOT HANDLED YET: $field - $id"
      }
    )
  }

  override fun name(id: ScheduleId): Flow<String?> = flow { emit(schedules.name(id)) }

  override fun names(field: Field, ids: List<String>): Flow<JsonArray> = flow {
    emit(
      when (field) {
        Field.Acct,
        Field.Account -> accounts.names(ids.map(::AccountId)).toJsonArray()

        Field.Category -> categories.names(ids.map(::CategoryId)).toJsonArray()

        Field.CategoryGroup -> categoryGroups.names(ids.map(::CategoryGroupId)).toJsonArray()

        Field.Description,
        Field.Payee -> payees.names(ids.map(::PayeeId)).toJsonArray()

        else -> buildJsonArray { add(JsonPrimitive("NOT HANDLED YET: $field - $ids")) }
      }
    )
  }

  private fun <T : Any> List<T>.toJsonArray(): JsonArray = buildJsonArray {
    forEach { item -> add(JsonPrimitive(item.toString())) }
  }
}
