package aktual.budget.rules.domain

import aktual.budget.model.Field
import aktual.budget.model.Operator
import kotlin.contracts.contract

// TYPE_INFO in packages/loot-core/src/shared/rules.ts
internal enum class FieldType(val ops: Set<Operator>) {
  Date([Is, IsApprox, GreaterThan, GreaterThanOrEquals, LessThan, LessThanOrEquals]),
  Id([Is, Contains, Matches, OneOf, IsNot, DoesNotContain, NotOneOf, OnBudget, OffBudget]),
  Saved([]),
  String([Is, Contains, Matches, OneOf, IsNot, DoesNotContain, NotOneOf, HasTags, HasAnyTag]),
  Number(
    [Is, IsApprox, IsBetween, GreaterThan, GreaterThanOrEquals, LessThan, LessThanOrEquals],
  ),
  Boolean([Is]),
}

// FIELD_INFO in packages/loot-core/src/shared/rules.ts, keyed by the public field names
internal enum class RuleField(val type: FieldType, val disallowedOps: Set<Operator> = []) {
  ImportedPayee(FieldType.String, disallowedOps = [HasTags, HasAnyTag]),
  Payee(Id, disallowedOps = [OnBudget, OffBudget]),
  PayeeName(FieldType.String),
  Date(FieldType.Date),
  Notes(FieldType.String, disallowedOps = [OneOf, NotOneOf]),
  Amount(FieldType.Number),
  Category(Id, disallowedOps = [OnBudget, OffBudget]),
  CategoryGroup(Id, disallowedOps = [OnBudget, OffBudget]),
  Account(Id),
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
    Account -> Account
    Amount -> Amount
    Category -> Category
    CategoryGroup -> CategoryGroup
    Date -> Date
    Description,
    Payee -> Payee
    Notes -> Notes
    PayeeName -> PayeeName
    ImportedDescription,
    ImportedPayee -> ImportedPayee
    Saved -> Saved
    Transfer -> Transfer
    Parent -> Parent
    Cleared -> Cleared
    Reconciled -> Reconciled
    Unknown -> null
  }

internal class RuleValidationException(message: String, cause: Throwable? = null) :
  IllegalArgumentException(message, cause)

internal inline fun ruleAssert(condition: Boolean, message: () -> String) {
  contract { returns() implies condition }
  if (!condition) throw RuleValidationException(message())
}
