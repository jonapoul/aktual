package aktual.budget.rules.vm

import aktual.budget.model.ConditionOptions
import aktual.budget.model.Field
import aktual.budget.model.Operator
import aktual.budget.model.RuleAction
import aktual.budget.model.RuleAction.Op.Set
import aktual.budget.model.RuleStage
import aktual.core.l10n.Res
import aktual.core.l10n.rules_field_account
import aktual.core.l10n.rules_field_amount
import aktual.core.l10n.rules_field_amount_inflow
import aktual.core.l10n.rules_field_amount_outflow
import aktual.core.l10n.rules_field_category
import aktual.core.l10n.rules_field_category_group
import aktual.core.l10n.rules_field_cleared
import aktual.core.l10n.rules_field_date
import aktual.core.l10n.rules_field_imported_payee
import aktual.core.l10n.rules_field_notes
import aktual.core.l10n.rules_field_parent
import aktual.core.l10n.rules_field_payee
import aktual.core.l10n.rules_field_payee_name
import aktual.core.l10n.rules_field_reconciled
import aktual.core.l10n.rules_field_saved
import aktual.core.l10n.rules_field_transfer
import aktual.core.l10n.rules_field_unknown
import aktual.core.l10n.rules_op_append_notes
import aktual.core.l10n.rules_op_delete_transaction
import aktual.core.l10n.rules_op_link_schedule
import aktual.core.l10n.rules_op_prepend_notes
import aktual.core.l10n.rules_op_set
import aktual.core.l10n.rules_op_set_split_amount
import aktual.core.l10n.rules_op_unknown
import aktual.core.l10n.rules_operator_contains
import aktual.core.l10n.rules_operator_does_not_contain
import aktual.core.l10n.rules_operator_greater_than
import aktual.core.l10n.rules_operator_greater_than_or_equals
import aktual.core.l10n.rules_operator_has_any_tag
import aktual.core.l10n.rules_operator_has_tags
import aktual.core.l10n.rules_operator_is
import aktual.core.l10n.rules_operator_is_approx
import aktual.core.l10n.rules_operator_is_between
import aktual.core.l10n.rules_operator_is_not
import aktual.core.l10n.rules_operator_less_than
import aktual.core.l10n.rules_operator_less_than_or_equals
import aktual.core.l10n.rules_operator_matches
import aktual.core.l10n.rules_operator_not_one_of
import aktual.core.l10n.rules_operator_off_budget
import aktual.core.l10n.rules_operator_on_budget
import aktual.core.l10n.rules_operator_one_of
import aktual.core.l10n.rules_stage_none
import aktual.core.l10n.rules_stage_post
import aktual.core.l10n.rules_stage_pre
import aktual.core.l10n.rules_stage_unknown
import org.jetbrains.compose.resources.StringResource

fun Field.label(options: ConditionOptions?): StringResource =
  when (this) {
    Account,
    Acct -> Res.string.rules_field_account
    Amount ->
      when {
        options?.inflow == true -> Res.string.rules_field_amount_inflow
        options?.outflow == true -> Res.string.rules_field_amount_outflow
        else -> Res.string.rules_field_amount
      }
    Category -> Res.string.rules_field_category
    CategoryGroup -> Res.string.rules_field_category_group
    Date -> Res.string.rules_field_date
    Notes -> Res.string.rules_field_notes
    Description,
    Payee -> Res.string.rules_field_payee
    PayeeName -> Res.string.rules_field_payee_name
    ImportedDescription,
    ImportedPayee -> Res.string.rules_field_imported_payee
    Saved -> Res.string.rules_field_saved
    Transfer -> Res.string.rules_field_transfer
    Parent -> Res.string.rules_field_parent
    Cleared -> Res.string.rules_field_cleared
    Reconciled -> Res.string.rules_field_reconciled
    Unknown -> Res.string.rules_field_unknown
  }

fun Operator.label(): StringResource =
  when (this) {
    Contains -> Res.string.rules_operator_contains
    DoesNotContain -> Res.string.rules_operator_does_not_contain
    GreaterThan -> Res.string.rules_operator_greater_than
    GreaterThanOrEquals -> Res.string.rules_operator_greater_than_or_equals
    HasAnyTag -> Res.string.rules_operator_has_any_tag
    HasTags -> Res.string.rules_operator_has_tags
    Is -> Res.string.rules_operator_is
    IsApprox -> Res.string.rules_operator_is_approx
    IsBetween -> Res.string.rules_operator_is_between
    IsNot -> Res.string.rules_operator_is_not
    LessThan -> Res.string.rules_operator_less_than
    LessThanOrEquals -> Res.string.rules_operator_less_than_or_equals
    Matches -> Res.string.rules_operator_matches
    NotOneOf -> Res.string.rules_operator_not_one_of
    OffBudget -> Res.string.rules_operator_off_budget
    OnBudget -> Res.string.rules_operator_on_budget
    OneOf -> Res.string.rules_operator_one_of
  }

fun RuleAction.Op.label(): StringResource =
  when (this) {
    AppendNotes -> Res.string.rules_op_append_notes
    DeleteTransaction -> Res.string.rules_op_delete_transaction
    LinkSchedule -> Res.string.rules_op_link_schedule
    PrependNotes -> Res.string.rules_op_prepend_notes
    Set -> Res.string.rules_op_set
    SetSplitAmount -> Res.string.rules_op_set_split_amount
    Unknown -> Res.string.rules_op_unknown
  }

fun RuleStage.label(): StringResource =
  when (this) {
    Pre -> Res.string.rules_stage_pre
    Default -> Res.string.rules_stage_none
    Post -> Res.string.rules_stage_post
    Unknown -> Res.string.rules_stage_unknown
  }
