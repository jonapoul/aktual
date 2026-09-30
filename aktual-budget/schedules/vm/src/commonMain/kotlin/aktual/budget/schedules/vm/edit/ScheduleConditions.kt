package aktual.budget.schedules.vm.edit

import aktual.budget.db.DbJson
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.Condition
import aktual.budget.model.ConditionType
import aktual.budget.model.Field
import aktual.budget.model.Operator
import aktual.budget.model.PayeeId
import aktual.budget.model.RecurConfig
import aktual.budget.model.RuleAction
import aktual.budget.model.ScheduleJsonPathIndex
import kotlin.math.roundToLong
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put

/** The conditions of a schedule's rule that make up the schedule itself */
internal data class ScheduleConditions(
  val payee: Condition?,
  val account: Condition?,
  val amount: Condition?,
  val date: Condition?,
) {
  fun asList(): List<Condition?> = listOf(payee, account, amount, date)
}

// packages/loot-core/src/shared/schedules.ts extractScheduleConds()
internal fun List<Condition>.scheduleConditions(): ScheduleConditions =
  ScheduleConditions(
    payee =
      firstOrNull { it.operator == Is && it.field == Payee }
        ?: firstOrNull { it.operator == Is && it.field == Description },
    account =
      firstOrNull { it.operator == Is && it.field == Account }
        ?: firstOrNull { it.operator == Is && it.field == Acct },
    amount = firstOrNull { it.field == Field.Amount && it.operator in AMOUNT_OPERATORS },
    date = firstOrNull { it.field == Date && it.operator in DATE_OPERATORS },
  )

private val AMOUNT_OPERATORS = setOf<Operator>(Is, IsApprox, IsBetween)
private val DATE_OPERATORS = setOf<Operator>(Is, IsApprox)

// packages/desktop-client/src/components/schedules/schedule-edit-utils.ts
// updateScheduleConditions(). Existing conditions keep their operator and only take the new
// value, except for the amount which is replaced outright.
internal fun ScheduleForm.toConditions(existing: List<Condition>): List<Condition> {
  val old = existing.scheduleConditions()

  fun update(cond: Condition?, field: Field, type: ConditionType, value: JsonElement): Condition? =
    when {
      cond != null -> cond.copy(value = value)
      value != JsonNull || field == Payee ->
        Condition(field = field, operator = Is, value = value, type = type)
      else -> null
    }

  val dateCondition =
    old.date?.copy(value = date.toJson())
      ?: Condition(
        field = Date,
        operator = IsApprox,
        value = date.toJson(),
        type = Date,
      )

  return listOfNotNull(
    update(old.payee, Payee, Id, payee?.value.toJson()),
    update(old.account, Account, Id, account?.value.toJson()),
    dateCondition,
    amount.toCondition(),
  )
}

private fun String?.toJson(): JsonElement = if (this == null) JsonNull else JsonPrimitive(this)

private fun ScheduleDate.toJson(): JsonElement =
  when (this) {
    is Once -> JsonPrimitive(date.toString())
    is Recurring -> DbJson.encodeToJsonElement(RecurConfig.serializer(), config)
  }

private fun ScheduleAmount.toCondition(): Condition {
  val (operator, value) =
    when (this) {
      is Exactly -> Operator.Is to JsonPrimitive(amount.toLong())
      is Approximately -> Operator.IsApprox to JsonPrimitive(amount.toLong())
      is Between -> Operator.IsBetween to betweenJson(from, to)
    }
  return Condition(
    field = Field.Amount,
    operator = operator,
    value = value,
    type = ConditionType.Number,
  )
}

private fun betweenJson(from: Amount, to: Amount): JsonElement = buildJsonObject {
  put("num1", from.toLong())
  put("num2", to.toLong())
}

// packages/loot-core/src/server/schedules/app.ts updateConditions(). Schedule conditions are
// swapped in place so any extra conditions added via "edit as rule" survive, and new ones go on
// the end.
internal fun mergeConditions(old: List<Condition>, new: List<Condition>): List<Condition> {
  val replacements = old.scheduleConditions().asList().zip(new.scheduleConditions().asList())
  val updated = old.map { cond ->
    replacements.firstOrNull { (before, _) -> before === cond }?.second ?: cond
  }
  val added = replacements.filter { (before, after) -> before == null && after != null }
  return updated + added.mapNotNull { it.second }
}

// packages/loot-core/src/server/schedules/app.ts updateActions(). Posting a schedule runs its rule,
// so a plain "set amount" action has to follow the amount condition or it would post the old
// amount. Returns null when nothing changed.
internal fun syncAmountActions(
  conditions: List<Condition>,
  actions: List<RuleAction>,
): List<RuleAction>? {
  val amountCondition = conditions.scheduleConditions().amount ?: return null
  val amount = scheduledAmount(amountCondition.value)

  var changed = false
  val updated = actions.map { action ->
    val options = action.options
    if (
      action.op == RuleAction.Op.Set &&
        action.field == Field.Amount &&
        options?.template == null &&
        options?.formula == null &&
        action.value?.longOrNull != amount
    ) {
      changed = true
      action.copy(value = JsonPrimitive(amount))
    } else {
      action
    }
  }
  return if (changed) updated else null
}

// packages/loot-core/src/shared/schedules.ts getScheduledAmount(), where "is between" amounts
// use the average
private fun scheduledAmount(value: JsonElement): Long =
  when (value) {
    is JsonPrimitive -> value.longOrNull ?: 0L
    is JsonObject -> {
      val num1 = value["num1"]?.jsonPrimitive?.longOrNull ?: 0L
      val num2 = value["num2"]?.jsonPrimitive?.longOrNull ?: 0L
      ((num1 + num2) / 2.0).roundToLong()
    }
    else -> 0L
  }

internal data class JsonPaths(
  val payee: ScheduleJsonPathIndex?,
  val account: ScheduleJsonPathIndex?,
  val amount: ScheduleJsonPathIndex?,
  val date: ScheduleJsonPathIndex?,
)

// packages/loot-core/src/server/schedules/app.ts onRuleUpdate()
internal fun List<Condition>.jsonPaths(): JsonPaths {
  val conditions = scheduleConditions()
  fun path(cond: Condition?): ScheduleJsonPathIndex? = indexOfFirst {
    it === cond
  }
    .takeIf { it >= 0 }
    ?.let { ScheduleJsonPathIndex(it) }
  return JsonPaths(
    payee = path(conditions.payee),
    account = path(conditions.account),
    amount = path(conditions.amount),
    date = path(conditions.date),
  )
}

/** Reads the form back out of a schedule's rule conditions. Missing parts fall back to defaults */
internal fun List<Condition>.toForm(
  name: String?,
  postsTransaction: Boolean,
  mappedPayee: PayeeId?,
  today: LocalDate,
): ScheduleForm {
  val conditions = scheduleConditions()
  return ScheduleForm(
    name = name.orEmpty(),
    payee = mappedPayee ?: conditions.payee?.value?.stringOrNull()?.let(::PayeeId),
    account = conditions.account?.value?.stringOrNull()?.let(::AccountId),
    amount = conditions.amount.toScheduleAmount(),
    date = conditions.date?.value?.toScheduleDate() ?: ScheduleDate.Once(today),
    postsTransaction = postsTransaction,
  )
}

private fun JsonElement.stringOrNull(): String? =
  (this as? JsonPrimitive)?.takeIf { it.isString }?.content?.takeIf { it.isNotBlank() }

private fun Condition?.toScheduleAmount(): ScheduleAmount {
  val value = this?.value
  return when (this?.operator) {
    IsBetween -> {
      val obj = value as? JsonObject
      val num1 = obj?.get("num1")?.jsonPrimitive?.longOrNull ?: 0L
      val num2 = obj?.get("num2")?.jsonPrimitive?.longOrNull ?: 0L
      ScheduleAmount.Between(Amount(num1), Amount(num2))
    }

    Is -> ScheduleAmount.Exactly(Amount(value.longOrZero()))

    else -> ScheduleAmount.Approximately(Amount(value.longOrZero()))
  }
}

private fun JsonElement?.longOrZero(): Long = (this as? JsonPrimitive)?.longOrNull ?: 0L

private fun JsonElement.toScheduleDate(): ScheduleDate? =
  when (this) {
    is JsonObject ->
      ScheduleDate.Recurring(DbJson.decodeFromJsonElement(RecurConfig.serializer(), this))
    is JsonPrimitive -> stringOrNull()?.let { ScheduleDate.Once(LocalDate.parse(it)) }
    else -> null
  }
