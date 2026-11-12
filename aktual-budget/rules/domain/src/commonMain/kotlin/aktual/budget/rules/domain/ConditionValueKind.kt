package aktual.budget.rules.domain

// How the condition editor edits a condition's value
enum class ConditionValueKind {
  Entity,
  EntityList,
  Text,
  Money,
  Day,
  Flag,
  NoValue,
}
