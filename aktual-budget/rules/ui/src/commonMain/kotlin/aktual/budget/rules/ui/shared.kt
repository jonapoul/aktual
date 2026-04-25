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
    Field.Account,
    Field.Acct -> Strings.rulesFieldAccount
    Field.Amount ->
      when {
        options?.inflow == true -> Strings.rulesFieldAmountInflow
        options?.outflow == true -> Strings.rulesFieldAmountOutflow
        else -> Strings.rulesFieldAmount
      }
    Field.Category -> Strings.rulesFieldCategory
    Field.CategoryGroup -> Strings.rulesFieldCategoryGroup
    Field.Date -> Strings.rulesFieldDate
    Field.Notes -> Strings.rulesFieldNotes
    Field.Description,
    Field.Payee -> Strings.rulesFieldPayee
    Field.PayeeName -> Strings.rulesFieldPayeeName
    Field.ImportedDescription,
    Field.ImportedPayee -> Strings.rulesFieldImportedPayee
    Field.Saved -> Strings.rulesFieldSaved
    Field.Transfer -> Strings.rulesFieldTransfer
    Field.Parent -> Strings.rulesFieldParent
    Field.Cleared -> Strings.rulesFieldCleared
    Field.Reconciled -> Strings.rulesFieldReconciled
  }

@Composable
internal fun Operator.displayString(): String =
  when (this) {
    Contains -> Strings.rulesOperatorContains
    DoesNotContain -> Strings.rulesOperatorDoesNotContain
    GreaterThan -> Strings.rulesOperatorGreaterThan
    GreaterThanOrEquals -> Strings.rulesOperatorGreaterThanOrEquals
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
    Field.Account -> operators<AccountOperator>()
    Field.Amount -> operators<AmountOperator>()
    Field.Category -> operators<CategoryOperator>()
    Field.CategoryGroup -> operators<CategoryGroupOperator>()
    Field.Date -> operators<DateOperator>()
    Field.Notes -> operators<NotesOperator>()
    Field.Payee -> operators<PayeeOperator>()
    Field.PayeeName -> operators<PayeeNameOperator>()
    Field.ImportedPayee -> operators<ImportedPayeeOperator>()
    Field.Saved -> operators<SavedOperator>()
    Field.Transfer -> operators<TransferOperator>()
    Field.Parent -> operators<ParentOperator>()
    Field.Cleared -> operators<ClearedOperator>()
    Field.Reconciled -> operators<ReconciledOperator>()

    // no operators
    Field.Acct,
    Field.ImportedDescription,
    Field.Description -> persistentListOf()
  }

private inline fun <reified O : Operator> operators(): ImmutableList<O> =
  ALL_OPERATORS.filterIsInstance<O>().toImmutableList()
