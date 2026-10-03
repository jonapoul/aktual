package aktual.budget.rules.domain

import aktual.budget.model.Field
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
import kotlin.contracts.contract

// TYPE_INFO in packages/loot-core/src/shared/rules.ts
internal enum class FieldType(val ops: Set<Operator>) {
  Date(setOf(Is, IsApprox, GreaterThan, GreaterThanOrEquals, LessThan, LessThanOrEquals)),
  Id(setOf(Is, Contains, Matches, OneOf, IsNot, DoesNotContain, NotOneOf, OnBudget, OffBudget)),
  Saved(emptySet()),
  String(setOf(Is, Contains, Matches, OneOf, IsNot, DoesNotContain, NotOneOf, HasTags, HasAnyTag)),
  Number(
    setOf(Is, IsApprox, IsBetween, GreaterThan, GreaterThanOrEquals, LessThan, LessThanOrEquals)
  ),
  Boolean(setOf(Is)),
}

// FIELD_INFO in packages/loot-core/src/shared/rules.ts, keyed by the public field names
internal enum class RuleField(val type: FieldType, val disallowedOps: Set<Operator> = emptySet()) {
  ImportedPayee(FieldType.String, disallowedOps = setOf(HasTags, HasAnyTag)),
  Payee(FieldType.Id, disallowedOps = setOf(OnBudget, OffBudget)),
  PayeeName(FieldType.String),
  Date(FieldType.Date),
  Notes(FieldType.String, disallowedOps = setOf(OneOf, NotOneOf)),
  Amount(FieldType.Number),
  Category(FieldType.Id, disallowedOps = setOf(OnBudget, OffBudget)),
  CategoryGroup(FieldType.Id, disallowedOps = setOf(OnBudget, OffBudget)),
  Account(FieldType.Id),
  Cleared(FieldType.Boolean),
  Reconciled(FieldType.Boolean),
  Saved(FieldType.Saved),
  Transfer(FieldType.Boolean),
  Parent(FieldType.Boolean);

  // isValidOp in packages/loot-core/src/shared/rules.ts
  fun isValidOp(op: Operator): Boolean = op !in disallowedOps && op in type.ops
}

/**
 * The public field for a stored one. Rules are saved with the `transactions` table's column names
 * where they differ (acct, description, imported_description), see `fromInternalField` in
 * packages/loot-core/src/server/transactions/transaction-rules.ts. Null for fields rules can't use.
 */
internal fun Field.toRuleField(): RuleField? =
  when (this) {
    Acct,
    Account -> RuleField.Account
    Amount -> RuleField.Amount
    Category -> RuleField.Category
    CategoryGroup -> RuleField.CategoryGroup
    Date -> RuleField.Date
    Description,
    Payee -> RuleField.Payee
    Notes -> RuleField.Notes
    PayeeName -> RuleField.PayeeName
    ImportedDescription,
    ImportedPayee -> RuleField.ImportedPayee
    Saved -> RuleField.Saved
    Transfer -> RuleField.Transfer
    Parent -> RuleField.Parent
    Cleared -> RuleField.Cleared
    Reconciled -> RuleField.Reconciled
    Unknown -> null
  }

internal class RuleValidationException(message: String, cause: Throwable? = null) :
  IllegalArgumentException(message, cause)

internal inline fun ruleAssert(condition: Boolean, message: () -> String) {
  contract { returns() implies condition }
  if (!condition) throw RuleValidationException(message())
}
