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
import aktual.budget.model.ParentOperator
import aktual.budget.model.PayeeNameOperator
import aktual.budget.model.PayeeOperator
import aktual.budget.model.ReconciledOperator
import aktual.budget.model.SavedOperator
import aktual.budget.model.TransferOperator
import aktual.budget.rules.vm.label
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun Field.string(options: ConditionOptions?): String = stringResource(label(options))

@Composable internal fun Operator.displayString(): String = stringResource(label())

private val ALL_OPERATORS =
  persistentListOf<Operator>(
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
