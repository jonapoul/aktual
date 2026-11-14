package aktual.budget.rules.domain

import aktual.budget.model.Condition
import aktual.budget.model.ConditionType
import aktual.budget.model.Field
import aktual.budget.model.Operator
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.longOrNull

// The fields a filter's conditions can use, in the order the editor offers them
val FILTER_FIELDS =
  persistentListOf<Field>(
    Account,
    Category,
    CategoryGroup,
    Payee,
    Notes,
    ImportedPayee,
    Date,
    Amount,
    Cleared,
    Reconciled,
    Transfer,
  )

private val ID_OPERATORS =
  persistentListOf<Operator>(Is, IsNot, OneOf, NotOneOf, Contains, DoesNotContain, Matches)

private val ACCOUNT_OPERATORS = ID_OPERATORS.adding(OnBudget).adding(OffBudget)

private val TEXT_OPERATORS =
  persistentListOf<Operator>(Is, IsNot, Contains, DoesNotContain, Matches)

private val COMPARE_OPERATORS =
  persistentListOf<Operator>(
    Is,
    IsApprox,
    GreaterThan,
    GreaterThanOrEquals,
    LessThan,
    LessThanOrEquals,
  )

private val FLAG_OPERATORS = persistentListOf<Operator>(Is)

fun filterOperators(field: Field): ImmutableList<Operator> =
  when (field) {
    Account -> ACCOUNT_OPERATORS
    Category,
    CategoryGroup,
    Payee -> ID_OPERATORS
    Notes,
    ImportedPayee -> TEXT_OPERATORS
    Date,
    Amount -> COMPARE_OPERATORS
    Cleared,
    Reconciled,
    Transfer -> FLAG_OPERATORS
    Acct,
    Description,
    ImportedDescription,
    PayeeName,
    Saved,
    Parent,
    Unknown -> persistentListOf()
  }

fun conditionValueKind(field: Field, op: Operator): ConditionValueKind =
  when (field) {
    Account,
    Category,
    CategoryGroup,
    Payee,
    Acct,
    Description -> idValueKind(op)
    Date -> Day
    Amount -> Money
    Cleared,
    Reconciled,
    Transfer,
    Parent,
    Saved -> Flag
    Notes,
    ImportedPayee,
    ImportedDescription,
    PayeeName,
    Unknown -> Text
  }

private fun idValueKind(op: Operator): ConditionValueKind =
  when (op) {
    OneOf,
    NotOneOf -> EntityList
    Contains,
    DoesNotContain,
    Matches -> Text
    OnBudget,
    OffBudget -> NoValue
    Is,
    IsNot,
    GreaterThan,
    GreaterThanOrEquals,
    HasAnyTag,
    HasTags,
    IsApprox,
    IsBetween,
    LessThan,
    LessThanOrEquals -> Entity
  }

// Null when the editor can't show the condition, like a recurring date or an amount range. Those
// are kept as they are, and can only be deleted
fun Condition.editableKind(): ConditionValueKind? {
  val flows = options?.inflow == true || options?.outflow == true
  if (customName != null || flows || operator !in filterOperators(field)) return null
  val kind = conditionValueKind(field, operator)
  return kind.takeIf { value.fits(it) }
}

private fun JsonElement.fits(kind: ConditionValueKind): Boolean =
  when (kind) {
    Entity -> this == JsonNull || isString()
    EntityList -> this is JsonArray && all { it.isString() }
    Text -> isString()
    Money -> this is JsonPrimitive && !isString && longOrNull != null
    Day -> isString() && (content().isEmpty() || content().toDayOrNull() != null)
    Flag -> this is JsonPrimitive && !isString && booleanOrNull != null
    NoValue -> true
  }

fun newFilterCondition(field: Field): Condition {
  val op = filterOperators(field).first()
  return Condition(
    field = field,
    operator = op,
    value = defaultValue(conditionValueKind(field, op)),
    type = field.conditionType(),
  )
}

// Keeps the value when the new operator takes the same kind
fun Condition.withOperator(op: Operator): Condition {
  val kind = conditionValueKind(field, op)
  val value = if (editableKind() == kind) value else defaultValue(kind)
  return copy(operator = op, value = value)
}

fun String.toDayOrNull(): LocalDate? = runCatching { LocalDate.parse(this) }.getOrNull()

private fun defaultValue(kind: ConditionValueKind): JsonElement =
  when (kind) {
    Entity,
    NoValue -> JsonNull
    EntityList -> JsonArray(emptyList())
    Text,
    Day -> JsonPrimitive("")
    Money -> JsonPrimitive(0)
    Flag -> JsonPrimitive(true)
  }

private fun Field.conditionType(): ConditionType =
  when (conditionValueKind(this, Is)) {
    Entity,
    EntityList,
    NoValue -> Id
    Text -> ConditionType.String
    Money -> ConditionType.Number
    Day -> Date
    Flag -> ConditionType.Boolean
  }

private fun JsonElement.isString(): Boolean = this is JsonPrimitive && isString

private fun JsonElement.content(): String = (this as JsonPrimitive).content
