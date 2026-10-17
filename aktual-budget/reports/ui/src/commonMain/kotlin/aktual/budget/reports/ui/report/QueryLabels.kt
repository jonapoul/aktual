package aktual.budget.reports.ui.report

import aktual.budget.model.Field
import aktual.budget.model.Operator
import aktual.budget.reports.vm.TimeFrameMode
import aktual.core.l10n.Strings
import androidx.compose.runtime.Composable

@Composable
internal fun Field.label(): String =
  when (this) {
    Account,
    Acct -> Strings.rulesFieldAccount
    Amount -> Strings.rulesFieldAmount
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
internal fun Operator.label(): String =
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
internal fun TimeFrameMode.label(): String =
  when (this) {
    SlidingWindow -> Strings.reportsDateConfigLive
    Static -> Strings.reportsDateConfigStatic
    Full -> Strings.reportsDateTypeAllTime
    LastMonth -> Strings.reportsDateTypeLastMonth
    LastYear -> Strings.reportsDateTypeLastYear
    YearToDate -> Strings.reportsDateTypeYearToDate
    PriorYearToDate -> Strings.reportsDateTypePriorYearToDate
    CurrentQuarter -> Strings.reportsDateTypeCurrentQuarter
    PreviousQuarter -> Strings.reportsDateTypePreviousQuarter
    Unknown -> Strings.reportsDateTypeUnknown
  }
