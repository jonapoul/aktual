package aktual.budget.rules.ui.list

import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.ConditionType
import aktual.budget.model.Field
import aktual.budget.model.Operator
import aktual.budget.model.RuleAction
import aktual.budget.model.RuleId
import aktual.budget.model.RuleStage
import aktual.budget.rules.vm.Rule
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.json.JsonPrimitive

internal val PreviewCondition1 =
  Condition(
    field = Field.Category,
    operator = Operator.Is,
    value = JsonPrimitive("condition-1-value"),
    options = null,
    conditionsOp = ConditionOp.And,
    type = ConditionType.String,
    customName = "My condition",
    queryFilter = null,
  )

internal val PreviewCondition2 =
  Condition(
    field = Field.ImportedPayee,
    operator = Operator.Contains,
    value = JsonPrimitive("amazon"),
    options = null,
    conditionsOp = null,
    type = ConditionType.String,
    customName = null,
    queryFilter = null,
  )

internal val PreviewRule1 =
  Rule(
    id = RuleId("item-1-id"),
    stage = RuleStage.Default,
    conditionsOp = ConditionOp.And,
    conditions = persistentListOf(PreviewCondition1),
    actions =
      persistentListOf(
        RuleAction(
          field = Field.Description,
          type = "payee",
          value = JsonPrimitive("0c76632b-d784-47b0-8391-d9c3067ad6fd"),
          op = RuleAction.Op.Set,
        )
      ),
  )

internal val PreviewRule2 =
  Rule(
    id = RuleId("item-2-id"),
    stage = RuleStage.Default,
    conditionsOp = ConditionOp.And,
    conditions = persistentListOf(PreviewCondition1, PreviewCondition2),
    actions =
      persistentListOf(
        RuleAction(
          value = JsonPrimitive("b08a2607-399b-4a6b-9a5c-3b2d083fe07f"),
          op = RuleAction.Op.LinkSchedule,
        )
      ),
  )
