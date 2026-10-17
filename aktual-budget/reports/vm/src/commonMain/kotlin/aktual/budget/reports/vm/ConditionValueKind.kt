package aktual.budget.reports.vm

// How the query editor edits a condition's value
enum class ConditionValueKind {
  Entity,
  EntityList,
  Text,
  Money,
  Day,
  Flag,
  NoValue,
}
