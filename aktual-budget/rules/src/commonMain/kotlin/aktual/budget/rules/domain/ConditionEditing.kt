package aktual.budget.rules.domain

import aktual.budget.model.AccountOperator
import aktual.budget.model.AmountOperator
import aktual.budget.model.CategoryGroupOperator
import aktual.budget.model.CategoryOperator
import aktual.budget.model.ClearedOperator
import aktual.budget.model.Condition
import aktual.budget.model.ConditionType
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
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
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

// Rules can use every field except the ones that duplicate another, like acct for account
val RULE_FIELDS: ImmutableList<Field> =
  Field.entries
    .filter { it !in setOf<Field>(Acct, Description, ImportedDescription, Unknown) }
    .toImmutableList()

/** Which conditions an editor offers: a filter's, as upstream's filters show them, or a rule's */
enum class ConditionSchema {
  FilterConditions,
  RuleConditions;

  val fields: ImmutableList<Field>
    get() =
      when (this) {
        FilterConditions -> FILTER_FIELDS
        RuleConditions -> RULE_FIELDS
      }

  fun operators(field: Field): ImmutableList<Operator> =
    when (this) {
      FilterConditions -> filterOperators(field)
      RuleConditions -> ruleOperators(field)
    }
}

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

private val DATE_OPERATORS =
  persistentListOf<Operator>(
    Is,
    IsApprox,
    IsBetween,
    GreaterThan,
    GreaterThanOrEquals,
    LessThan,
    LessThanOrEquals,
  )

private val FLAG_OPERATORS = persistentListOf<Operator>(Is)

private val RECUR_OPERATORS = setOf<Operator>(Is, IsApprox)

fun filterOperators(field: Field): ImmutableList<Operator> =
  when (field) {
    Account -> ACCOUNT_OPERATORS
    Category,
    CategoryGroup,
    Payee -> ID_OPERATORS
    Notes,
    ImportedPayee -> TEXT_OPERATORS
    Date -> DATE_OPERATORS
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

fun ruleOperators(field: Field): ImmutableList<Operator> =
  when (field) {
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
    Acct,
    ImportedDescription,
    Description,
    Unknown -> persistentListOf()
  }

private inline fun <reified O : Operator> operators(): ImmutableList<Operator> =
  ALL_OPERATORS.filterIsInstance<O>().toImmutableList()

fun conditionValueKind(field: Field, op: Operator): ConditionValueKind =
  when (field) {
    Account,
    Category,
    CategoryGroup,
    Payee,
    Acct,
    Description -> idValueKind(op)
    Date -> if (op == IsBetween) DayRange else Day
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
    Unknown -> textValueKind(op)
  }

private fun textValueKind(op: Operator): ConditionValueKind =
  if (op == OneOf || op == NotOneOf) TextList else Text

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

// Null when the editor can't show the condition, like an amount range or a custom name. Those
// are kept as they are, and can only be deleted
fun Condition.editableKind(schema: ConditionSchema = FilterConditions): ConditionValueKind? {
  val flows = options?.inflow == true || options?.outflow == true
  if (customName != null || flows || operator !in operators(schema)) return null
  val kind = valueKind()
  return kind.takeIf { value.fits(it) }
}

// Upstream only offers is and isapprox on a recurring date, which a schedule's rule has
fun Condition.operators(schema: ConditionSchema = FilterConditions): ImmutableList<Operator> {
  val operators = schema.operators(field)
  return if (valueKind() == Recurrence) {
    operators.filter { it in RECUR_OPERATORS }.toImmutableList()
  } else {
    operators
  }
}

private fun Condition.valueKind(op: Operator = operator): ConditionValueKind =
  if (field == Date && op != IsBetween && value is JsonObject) {
    Recurrence
  } else {
    conditionValueKind(field, op)
  }

private fun JsonElement.fits(kind: ConditionValueKind): Boolean =
  when (kind) {
    Entity -> this == JsonNull || isString()
    EntityList,
    TextList -> this is JsonArray && all { it.isString() }
    Text -> isString()
    Money -> this is JsonPrimitive && !isString && longOrNull != null
    Day -> isDayOrEmpty()
    DayRange -> this is JsonObject && DAY_RANGE_KEYS.all { get(it)?.isDayOrEmpty() == true }
    Recurrence -> toRecurConfigOrNull() != null
    Flag -> this is JsonPrimitive && !isString && booleanOrNull != null
    NoValue -> true
  }

fun newCondition(field: Field, schema: ConditionSchema = FilterConditions): Condition {
  val op = schema.operators(field).first()
  return Condition(
    field = field,
    operator = op,
    value = defaultValue(conditionValueKind(field, op)),
    type = field.conditionType(),
  )
}

// Carries the value over to the new operator where it can, so "one of A, B" becomes "is A" and a
// payee "is X" becomes "contains" X's name. [names] maps ids to names for that
fun Condition.withOperator(
  op: Operator,
  schema: ConditionSchema = FilterConditions,
  names: Map<String, String> = emptyMap(),
): Condition {
  val kind = valueKind(op)
  val from = editableKind(schema)
  val value = from?.let { value.convert(it, kind, names) } ?: defaultValue(kind)
  return copy(operator = op, value = value)
}

private fun JsonElement.convert(
  from: ConditionValueKind,
  to: ConditionValueKind,
  names: Map<String, String>,
): JsonElement? {
  if (from == to) return this
  if (from == Day && to == DayRange) return dayRange(this, this)
  if (from !in STRING_KINDS || to !in STRING_KINDS) return null
  // Text can't become an id without picking one
  if (from !in ID_KINDS && to in ID_KINDS) return null

  val strings =
    when (this) {
        is JsonArray -> map { it.content() }
        is JsonPrimitive -> listOfNotNull(contentOrNull)
        is JsonObject -> emptyList()
      }
      .let { if (from in ID_KINDS && to !in ID_KINDS) it.mapNotNull(names::get) else it }
      .filter { it.isNotEmpty() }

  return when (to) {
    EntityList,
    TextList -> JsonArray(strings.map(::JsonPrimitive))
    Entity -> strings.firstOrNull()?.let(::JsonPrimitive) ?: JsonNull
    Text -> JsonPrimitive(strings.firstOrNull().orEmpty())
    Money,
    Day,
    DayRange,
    Recurrence,
    Flag,
    NoValue -> null
  }
}

private val ID_KINDS = setOf<ConditionValueKind>(Entity, EntityList)

private val STRING_KINDS = ID_KINDS + setOf<ConditionValueKind>(Text, TextList)

private fun defaultValue(kind: ConditionValueKind): JsonElement =
  when (kind) {
    Entity,
    NoValue -> JsonNull
    EntityList,
    TextList -> JsonArray(emptyList())
    Text,
    Day,
    Recurrence -> JsonPrimitive("")
    DayRange -> dayRange(JsonPrimitive(""), JsonPrimitive(""))
    Money -> JsonPrimitive(0)
    Flag -> JsonPrimitive(true)
  }

private fun Field.conditionType(): ConditionType =
  when (conditionValueKind(this, Is)) {
    Entity,
    EntityList,
    NoValue -> Id
    Text,
    TextList -> ConditionType.String
    Money -> ConditionType.Number
    Day,
    DayRange,
    Recurrence -> Date
    Flag -> ConditionType.Boolean
  }

private fun JsonElement.isString(): Boolean = this is JsonPrimitive && isString

private fun JsonElement.isDayOrEmpty(): Boolean =
  isString() && (content().isEmpty() || content().toDayOrNull() != null)

// The value of a date isbetween condition, as upstream stores it
fun dayRange(start: JsonElement, end: JsonElement): JsonObject =
  JsonObject(mapOf(DAY_RANGE_START to start, DAY_RANGE_END to end))

const val DAY_RANGE_START = "num1"
const val DAY_RANGE_END = "num2"
private val DAY_RANGE_KEYS = listOf(DAY_RANGE_START, DAY_RANGE_END)

private fun JsonElement.content(): String = (this as JsonPrimitive).content
