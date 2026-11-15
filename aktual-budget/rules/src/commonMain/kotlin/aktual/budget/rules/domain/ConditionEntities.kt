package aktual.budget.rules.domain

import aktual.budget.model.Field
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow

@Immutable data class ConditionEntity(val id: String, val name: String)

// What the condition editor picks from
@Stable
interface ConditionEntities {
  val accounts: ImmutableList<ConditionEntity>
  val categories: ImmutableList<ConditionEntity>
  val categoryGroups: ImmutableList<ConditionEntity>
  val payees: ImmutableList<ConditionEntity>

  // Every entity's name by id, including hidden categories and closed accounts that saved
  // conditions can still point to
  val names: ImmutableMap<String, String>
}

interface ConditionEntitiesLoader {
  // Loads again whenever an account, category, group or payee changes
  fun observe(): Flow<ConditionEntities>

  suspend fun load(): ConditionEntities
}

fun ConditionEntities.entities(field: Field): ImmutableList<ConditionEntity> =
  when (field) {
    Account,
    Acct -> accounts
    Category -> categories
    CategoryGroup -> categoryGroups
    Payee,
    Description -> payees
    Amount,
    Date,
    Notes,
    PayeeName,
    ImportedDescription,
    ImportedPayee,
    Saved,
    Transfer,
    Parent,
    Cleared,
    Reconciled,
    Unknown -> persistentListOf()
  }

@Immutable
data class ConditionEntityLists(
  override val accounts: ImmutableList<ConditionEntity>,
  override val categories: ImmutableList<ConditionEntity>,
  override val categoryGroups: ImmutableList<ConditionEntity>,
  override val payees: ImmutableList<ConditionEntity>,
  override val names: ImmutableMap<String, String>,
) : ConditionEntities
