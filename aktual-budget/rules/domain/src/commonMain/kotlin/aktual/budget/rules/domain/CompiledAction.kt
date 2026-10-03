@file:Suppress("BracesOnWhenStatements")

package aktual.budget.rules.domain

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.model.RuleAction
import aktual.budget.model.ScheduleId
import kotlin.math.roundToLong
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull

/**
 * A validated rule action. Port of `Action` in packages/loot-core/src/server/rules/action.ts: the
 * constructor's validation is [compile], and `Action.exec` is [exec].
 */
internal sealed interface CompiledAction {
  // Zero for actions on the whole transaction, otherwise the 1-based split the action builds
  val splitIndex: Int

  fun exec(subject: RuleSubject): RuleSubject

  data class SetField(
    val field: RuleField,
    val value: JsonPrimitive?,
    override val splitIndex: Int,
  ) : CompiledAction {
    @Suppress("CyclomaticComplexMethod")
    override fun exec(subject: RuleSubject): RuleSubject {
      val transaction = subject.transaction
      return when (field) {
        RuleField.Account ->
          subject.copy(transaction = transaction.copy(account = AccountId(value.text().orEmpty())))
        RuleField.Category ->
          subject.copy(transaction = transaction.copy(category = value.id(::CategoryId)))
        RuleField.Payee ->
          subject.copy(
            transaction = transaction.copy(payee = value.id(::PayeeId)),
            newPayeePending = false,
          )
        RuleField.PayeeName ->
          // The payee is looked up by name once the rule has finished
          subject.copy(
            transaction = transaction.copy(payee = null),
            payeeName = value.text(),
            newPayeePending = true,
          )
        RuleField.ImportedPayee ->
          subject.copy(transaction = transaction.copy(importedPayee = value.text()))
        RuleField.Notes -> subject.copy(transaction = transaction.copy(notes = value.text()))
        RuleField.Date ->
          value.date()?.let { subject.copy(transaction = transaction.copy(date = it)) } ?: subject
        RuleField.Amount ->
          value.amount()?.let { subject.copy(transaction = transaction.copy(amount = it)) }
            ?: subject
        RuleField.Cleared ->
          value.flag()?.let { subject.copy(transaction = transaction.copy(cleared = it)) }
            ?: subject
        RuleField.Reconciled ->
          value.flag()?.let { subject.copy(transaction = transaction.copy(reconciled = it)) }
            ?: subject
        // Upstream writes this onto the transaction, where later category_group conditions read it
        // until the category next changes
        RuleField.CategoryGroup -> subject.copy(categoryGroup = value.id(::CategoryGroupId))
        // Not transaction fields: upstream's write is dropped when the transaction is saved
        RuleField.Saved,
        RuleField.Transfer,
        RuleField.Parent -> subject
      }
    }
  }

  data class SetSplitAmount(
    val method: RuleAction.Method?,
    val value: Double?,
    override val splitIndex: Int,
  ) : CompiledAction {
    // Percentages and remainders are shared out once every split exists, see RulesEngine
    override fun exec(subject: RuleSubject): RuleSubject =
      if (method == FixedAmount && value != null) {
        subject.copy(transaction = subject.transaction.copy(amount = Amount(value.roundToLong())))
      } else {
        subject
      }
  }

  data class LinkSchedule(val schedule: ScheduleId?, override val splitIndex: Int) :
    CompiledAction {
    override fun exec(subject: RuleSubject) =
      subject.copy(transaction = subject.transaction.copy(schedule = schedule))
  }

  data class PrependNotes(val text: String, override val splitIndex: Int) : CompiledAction {
    override fun exec(subject: RuleSubject): RuleSubject {
      val notes = subject.transaction.notes
      val updated = if (notes.isNullOrEmpty()) text else text + notes
      return subject.copy(transaction = subject.transaction.copy(notes = updated))
    }
  }

  data class AppendNotes(val text: String, override val splitIndex: Int) : CompiledAction {
    override fun exec(subject: RuleSubject): RuleSubject {
      val notes = subject.transaction.notes
      val updated = if (notes.isNullOrEmpty()) text else notes + text
      return subject.copy(transaction = subject.transaction.copy(notes = updated))
    }
  }

  data class DeleteTransaction(override val splitIndex: Int) : CompiledAction {
    override fun exec(subject: RuleSubject) =
      subject.copy(transaction = subject.transaction.copy(tombstone = true))
  }

  // TODO(#1681): Handlebars templates and HyperFormula formulas aren't supported, so these
  // actions are skipped
  data class Unsupported(val reason: String, override val splitIndex: Int) : CompiledAction {
    override fun exec(subject: RuleSubject) = subject
  }

  companion object {
    /** Validates [action] the way upstream's `Action` constructor does. */
    fun compile(action: RuleAction, idMappings: Map<String, String>): CompiledAction {
      val splitIndex = action.options?.splitIndex ?: 0
      val value = action.value?.takeIf { it != JsonNull }
      return when (action.op) {
        RuleAction.Op.Set -> compileSet(action, value, splitIndex, idMappings)
        RuleAction.Op.SetSplitAmount -> {
          val method = action.options?.method
          if (method == Formula) {
            Unsupported("split amount formula", splitIndex)
          } else {
            SetSplitAmount(method, value?.takeIf { !it.isString }?.doubleOrNull, splitIndex)
          }
        }
        RuleAction.Op.LinkSchedule -> LinkSchedule(value.id(::ScheduleId), splitIndex)
        RuleAction.Op.PrependNotes -> PrependNotes(value.text().orEmpty(), splitIndex)
        RuleAction.Op.AppendNotes -> AppendNotes(value.text().orEmpty(), splitIndex)
        RuleAction.Op.DeleteTransaction -> DeleteTransaction(splitIndex)
        RuleAction.Op.Unknown ->
          throw RuleValidationException("Invalid action operation: ${action.op}")
      }
    }

    private fun compileSet(
      action: RuleAction,
      value: JsonPrimitive?,
      splitIndex: Int,
      idMappings: Map<String, String>,
    ): CompiledAction {
      val field = action.field?.toRuleField()
      ruleAssert(field != null) { "Invalid field for action: ${action.field}" }
      if (field == RuleField.Account) {
        ruleAssert(!value.text().isNullOrEmpty()) { "Field cannot be empty: $field" }
      }

      val options = action.options
      return when {
        !options?.formula.isNullOrEmpty() -> Unsupported("formula for $field", splitIndex)
        !options?.template.isNullOrEmpty() -> Unsupported("template for $field", splitIndex)
        field.type == Id -> {
          val id = value.text()
          val mapped = id?.let { idMappings[it] }?.let(::JsonPrimitive) ?: value
          SetField(field, mapped, splitIndex)
        }
        else -> SetField(field, value, splitIndex)
      }
    }
  }
}

private fun JsonPrimitive?.text(): String? = this?.contentOrNull

// Empty ids are treated as clearing the field
private fun <T> JsonPrimitive?.id(constructor: (String) -> T): T? =
  text()?.takeIf { it.isNotEmpty() }?.let(constructor)

private fun JsonPrimitive?.date(): LocalDate? =
  text()?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

private fun JsonPrimitive?.amount(): Amount? {
  if (this == null || isString) return null
  return longOrNull?.let(::Amount) ?: doubleOrNull?.roundToLong()?.let(::Amount)
}

private fun JsonPrimitive?.flag(): Boolean? = this?.takeIf { !it.isString }?.booleanOrNull
