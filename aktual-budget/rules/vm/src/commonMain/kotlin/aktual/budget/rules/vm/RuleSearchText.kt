@file:Suppress("BracesOnWhenStatements")

package aktual.budget.rules.vm

import aktual.budget.model.Condition
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
    val field = condition.field
    when (val value = condition.value) {
      is JsonArray -> {
        val ids = value.map { it.jsonPrimitive.content }
        if (field.isIdField()) {
          addAll(nameFetcher.names(field, ids).first().map { it.jsonPrimitive.content })
        } else {
          addAll(ids)
        }
      }

      is JsonPrimitive -> {
        val content = value.content
        add(if (field.isIdField()) nameFetcher.name(field, content).first() ?: content else content)
      }

      else -> Unit
    }
  }

  private suspend fun actionText(action: RuleAction): List<String> = buildList {
    add(strings.get(action.op.label()))
    action.field?.let { add(strings.get(it.label(options = null))) }
    val content = action.value?.content ?: return@buildList
    when (action.op) {
      Set -> {
        val field = action.field
        add(if (field.isIdField()) nameFetcher.name(field, content).first() ?: content else content)
      }

      LinkSchedule -> nameFetcher.name(ScheduleId(content)).first()?.let(::add)

      AppendNotes,
      PrependNotes,
      SetSplitAmount -> add(content)

      DeleteTransaction,
      Unknown -> Unit
    }
  }
}
