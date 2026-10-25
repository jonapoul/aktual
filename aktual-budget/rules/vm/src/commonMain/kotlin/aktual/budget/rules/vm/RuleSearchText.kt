package aktual.budget.rules.vm

import aktual.budget.model.Condition
import aktual.budget.model.Field
import aktual.budget.model.RuleAction
import aktual.budget.model.RuleAction.Op.Set
import aktual.budget.model.ScheduleId
import aktual.budget.model.isIdField
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

// Mirrors ruleToString in packages/desktop-client/src/components/ManageRules.tsx
@Inject
internal class RuleSearchText(
  private val nameFetcher: NameFetcher,
  private val strings: StringLookup,
) {
  suspend fun of(rule: Rule): String = buildList {
    if (rule.stage == Pre || rule.stage == Post) add(strings.get(rule.stage.label()))
    rule.conditions.forEach { addAll(conditionText(it)) }
    rule.actions.forEach { addAll(actionText(it)) }
  }
    .joinToString(separator = " ")

  private suspend fun conditionText(condition: Condition): List<String> = buildList {
    add(strings.get(condition.field.label(condition.options)))
    add(strings.get(condition.operator.label()))
    when (val value = condition.value) {
      is JsonArray -> addAll(names(condition.field, value.map { it.jsonPrimitive.content }))
      is JsonPrimitive -> add(name(condition.field, value.content))
      else -> Unit
    }
  }

  private suspend fun actionText(action: RuleAction): List<String> = buildList {
    add(strings.get(action.op.label()))
    action.field?.let { add(strings.get(it.label(options = null))) }
    val content = action.value?.content ?: return@buildList
    when (action.op) {
      Set -> add(name(action.field, content))
      LinkSchedule -> nameFetcher.name(ScheduleId(content)).first()?.let(::add)
      AppendNotes,
      PrependNotes,
      SetSplitAmount -> add(content)
      DeleteTransaction,
      Unknown -> Unit
    }
  }

  private suspend fun name(field: Field?, value: String): String =
    if (field.isIdField()) nameFetcher.name(field, value).first() ?: value else value

  private suspend fun names(field: Field, values: List<String>): List<String> =
    if (field.isIdField()) {
      nameFetcher.names(field, values).first().map { it.jsonPrimitive.content }
    } else {
      values
    }
}
