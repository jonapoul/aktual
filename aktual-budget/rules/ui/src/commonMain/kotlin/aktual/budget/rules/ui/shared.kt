package aktual.budget.rules.ui

import aktual.budget.model.AccountOperator
import aktual.budget.model.AmountOperator
import aktual.budget.model.CategoryGroupOperator
import aktual.budget.model.CategoryOperator
import aktual.budget.model.ClearedOperator
import aktual.budget.model.Condition
import aktual.budget.model.ConditionOptions
import aktual.budget.model.DateOperator
import aktual.budget.model.Field
import aktual.budget.model.ImportedPayeeOperator
import aktual.budget.model.NotesOperator
import aktual.budget.model.Operator
import aktual.budget.model.Operator.Contains
import aktual.budget.model.Operator.DoesNotContain
import aktual.budget.model.Operator.GreaterThan
import aktual.budget.model.Operator.GreaterThanOrEquals
import aktual.budget.model.Operator.HasAnyTag
import aktual.budget.model.Operator.HasTags
import aktual.budget.model.Operator.Is
import aktual.budget.model.Operator.IsApprox
import aktual.budget.model.Operator.IsBetween
import aktual.budget.model.Operator.IsNot
import aktual.budget.model.Operator.LessThan
import aktual.budget.model.Operator.LessThanOrEquals
import aktual.budget.model.Operator.Matches
import aktual.budget.model.Operator.NotOneOf
import aktual.budget.model.Operator.OffBudget
import aktual.budget.model.Operator.OnBudget
import aktual.budget.model.Operator.OneOf
import aktual.budget.model.ParentOperator
import aktual.budget.model.PayeeNameOperator
import aktual.budget.model.PayeeOperator
import aktual.budget.model.ReconciledOperator
import aktual.budget.model.SavedOperator
import aktual.budget.model.TransferOperator
import aktual.core.l10n.Strings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun Field.string(options: ConditionOptions?): String =
  when (this) {
    Account,
    Acct -> Strings.rulesFieldAccount
    Amount ->
      when {
        options?.inflow == true -> Strings.rulesFieldAmountInflow
        options?.outflow == true -> Strings.rulesFieldAmountOutflow
        else -> Strings.rulesFieldAmount
      }
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
internal fun Operator.displayString(): String =
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

private val ALL_OPERATORS =
  persistentListOf(
    Contains,
    DoesNotContain,
    GreaterThan,
    GreaterThanOrEquals,
    HasAnyTag,
    HasTags,
    Is,
    IsApprox,
    IsBetween,
    IsNot,
    LessThan,
    LessThanOrEquals,
    Matches,
    NotOneOf,
    OffBudget,
    OnBudget,
    OneOf,
  )

@Stable
internal fun filteredOperators(condition: Condition): ImmutableList<Operator> =
  when (condition.field) {
    Account -> operators<AccountOperator>()
    Amount -> operators<AmountOperator>()
    Category -> operators<CategoryOperator>()
    CategoryGroup -> operators<CategoryGroupOperator>()
    Date -> operators<DateOperator>()
    Notes -> operators<NotesOperator>()
    Payee -> operators<PayeeOperator>()
    PayeeName -> operators<PayeeNameOperator>()
    ImportedPayee -> operators<ImportedPayeeOperator>()
    Saved -> operators<SavedOperator>()
    Transfer -> operators<TransferOperator>()
    Parent -> operators<ParentOperator>()
    Cleared -> operators<ClearedOperator>()
    Reconciled -> operators<ReconciledOperator>()

    // no operators
    Acct,
    ImportedDescription,
    Description,
    Unknown -> persistentListOf()
  }

private inline fun <reified O : Operator> operators(): ImmutableList<O> =
  ALL_OPERATORS.filterIsInstance<O>().toImmutableList()
