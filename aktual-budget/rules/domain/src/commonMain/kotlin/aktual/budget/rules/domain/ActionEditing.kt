package aktual.budget.rules.domain

import aktual.budget.model.Field
import aktual.budget.model.RuleAction
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.longOrNull

// The fields a rule's actions can set, in the order upstream's editor offers them
val ACTION_FIELDS =
  persistentListOf<Field>(Category, Payee, PayeeName, Notes, Cleared, Account, Date, Amount)

private val PARENT_ONLY_FIELDS = setOf<Field>(Amount, Cleared, Account, Date)

// Splits can't change what belongs to the whole transaction
val SPLIT_ACTION_FIELDS: ImmutableList<Field> =
  ACTION_FIELDS.filter { it !in PARENT_ONLY_FIELDS }.toImmutableList()

// Linking a schedule is only done from the schedules screen, as upstream does
val ACTION_OPS =
  persistentListOf(
    RuleAction.Op.Set,
    PrependNotes,
    AppendNotes,
    DeleteTransaction,
  )

// Zero for the whole transaction, otherwise the 1-based split the action builds
val RuleAction.splitIndex: Int
  get() = options?.splitIndex ?: 0

fun actionFields(splitIndex: Int): ImmutableList<Field> =
  if (splitIndex == 0) ACTION_FIELDS else SPLIT_ACTION_FIELDS

fun actionValueKind(field: Field): ConditionValueKind =
  when (field) {
    Account,
    Category,
    CategoryGroup,
    Payee,
    Acct,
    Description -> Entity
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

// Null when the editor can't show the action, like a template, a formula or a linked schedule.
// Those are kept as they are, and can only be deleted. Split amounts are edited with their split
fun RuleAction.editableKind(): ConditionValueKind? {
  if (options?.template != null || options?.formula != null) return null
  val kind: ConditionValueKind? =
    when (op) {
      RuleAction.Op.Set -> field?.takeIf { it in actionFields(splitIndex) }?.let(::actionValueKind)
      PrependNotes,
      AppendNotes -> Text
      DeleteTransaction -> NoValue
      SetSplitAmount,
      LinkSchedule,
      Unknown -> null
    }
  return kind?.takeIf { value.fits(it) }
}

private fun JsonPrimitive?.fits(kind: ConditionValueKind): Boolean =
  when (kind) {
    Entity -> this == null || this == JsonNull || isString
    Text -> this == null || this == JsonNull || isString
    Money -> this != null && !isString && longOrNull != null
    Day -> this != null && isString && (content.isEmpty() || content.toDayOrNull() != null)
    Flag -> this != null && !isString && booleanOrNull != null
    NoValue -> true
    EntityList -> false
  }

fun newAction(field: Field, splitIndex: Int = 0): RuleAction =
  RuleAction(
    op = RuleAction.Op.Set,
    field = field,
    type = field.actionType(),
    value = defaultActionValue(actionValueKind(field)),
    options = splitOptions(splitIndex),
  )

// The first field this split doesn't set yet, as upstream picks for a new action
fun List<RuleAction>.nextActionField(splitIndex: Int): Field {
  val used = filter { it.splitIndex == splitIndex }.mapNotNull { it.field }.toSet()
  return actionFields(splitIndex).firstOrNull { it !in used } ?: Category
}

// The field and its type stay when switching to the notes ops, as upstream keeps them
fun RuleAction.withOp(op: RuleAction.Op): RuleAction {
  if (op == this.op) return this
  val cleared = options?.copy(template = null, formula = null)
  return when (op) {
    RuleAction.Op.Set -> {
      val field =
        field?.takeIf { it in actionFields(splitIndex) } ?: actionFields(splitIndex).first()
      newAction(field, splitIndex).copy(options = cleared ?: splitOptions(splitIndex))
    }
    PrependNotes,
    AppendNotes -> copy(op = op, value = JsonPrimitive(""), options = cleared)
    DeleteTransaction -> copy(op = op, value = JsonPrimitive(""), options = cleared)
    SetSplitAmount,
    LinkSchedule,
    Unknown -> copy(op = op)
  }
}

fun RuleAction.withField(field: Field): RuleAction {
  if (field == this.field) return this
  return copy(
    field = field,
    type = field.actionType(),
    value = defaultActionValue(actionValueKind(field)),
    options = options?.copy(template = null, formula = null),
  )
}

private fun splitOptions(splitIndex: Int): RuleAction.Options? =
  if (splitIndex == 0) null else RuleAction.Options(splitIndex = splitIndex)

private fun defaultActionValue(kind: ConditionValueKind): JsonPrimitive? =
  when (kind) {
    Entity,
    EntityList,
    NoValue -> null
    Text,
    Day -> JsonPrimitive("")
    Money -> JsonPrimitive(0)
    Flag -> JsonPrimitive(true)
  }

// FIELD_TYPES in packages/loot-core/src/shared/rules.ts
private fun Field.actionType(): RuleAction.Type =
  when (this) {
    Account,
    Acct,
    Category,
    CategoryGroup,
    Description,
    Payee -> Id
    Date -> Date
    Amount -> RuleAction.Type.Number
    Cleared,
    Reconciled,
    Transfer,
    Parent -> RuleAction.Type.Boolean
    Saved -> Saved
    Notes,
    ImportedPayee,
    ImportedDescription,
    PayeeName -> RuleAction.Type.String
    Unknown -> Unknown
  }
