package aktual.budget.rules.domain

import aktual.budget.model.Field
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf

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
