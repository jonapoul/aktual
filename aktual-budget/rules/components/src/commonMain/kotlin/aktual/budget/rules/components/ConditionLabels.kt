package aktual.budget.rules.components

import aktual.budget.model.Amount
import aktual.budget.model.Condition
import aktual.budget.model.Field
import aktual.budget.model.Operator
import aktual.budget.rules.domain.editableKind
import aktual.core.l10n.Strings
import aktual.core.ui.formattedString
import androidx.compose.runtime.Composable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

@Composable
fun Field.label(): String =
  when (this) {
    Account,
    Acct -> Strings.rulesFieldAccount
    Field.Amount -> Strings.rulesFieldAmount
    Category -> Strings.rulesFieldCategory
    CategoryGroup -> Strings.rulesFieldCategoryGroup
    Date -> Strings.rulesFieldDate
    Notes -> Strings.rulesFieldNotes
    Description,
    Payee -> Strings.rulesFieldPayee
    PayeeName -> Strings.rulesFieldPayeeName
    ImportedDescription,
    ImportedPayee -> Strings.rulesFieldImportedPayee
    Saved -> Strings.rulesFieldSaved
    Transfer -> Strings.rulesFieldTransfer
    Parent -> Strings.rulesFieldParent
    Cleared -> Strings.rulesFieldCleared
    Reconciled -> Strings.rulesFieldReconciled
    Unknown -> Strings.rulesFieldUnknown
  }

@Composable
fun Operator.label(): String =
  when (this) {
    Contains -> Strings.rulesOperatorContains
    DoesNotContain -> Strings.rulesOperatorDoesNotContain
    GreaterThan -> Strings.rulesOperatorGreaterThan
    GreaterThanOrEquals -> Strings.rulesOperatorGreaterThanOrEquals
    HasAnyTag -> Strings.rulesOperatorHasAnyTag
    HasTags -> Strings.rulesOperatorHasTags
    Is -> Strings.rulesOperatorIs
    IsApprox -> Strings.rulesOperatorIsApprox
    IsBetween -> Strings.rulesOperatorIsBetween
    IsNot -> Strings.rulesOperatorIsNot
    LessThan -> Strings.rulesOperatorLessThan
    LessThanOrEquals -> Strings.rulesOperatorLessThanOrEquals
    Matches -> Strings.rulesOperatorMatches
    NotOneOf -> Strings.rulesOperatorNotOneOf
    OffBudget -> Strings.rulesOperatorOffBudget
    OnBudget -> Strings.rulesOperatorOnBudget
    OneOf -> Strings.rulesOperatorOneOf
  }

@Composable
fun Condition.valueLabel(names: ImmutableMap<String, String>): String? =
  when (editableKind()) {
    Entity ->
      value.jsonPrimitive.contentOrNull?.let { names.nameOf(it) }
        ?: Strings.reportsFormulaQueryNothing
    EntityList -> (value as JsonArray).map { names.nameOf(it.jsonPrimitive.content) }.joinToString()
    Text,
    Day -> value.jsonPrimitive.content
    Money -> Amount(value.jsonPrimitive.long).formattedString()
    Flag ->
      if (value.jsonPrimitive.boolean) {
        Strings.reportsFormulaQueryTrue
      } else {
        Strings.reportsFormulaQueryFalse
      }
    NoValue -> null
    null -> value.toString()
  }
