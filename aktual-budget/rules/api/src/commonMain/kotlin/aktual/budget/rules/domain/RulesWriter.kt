package aktual.budget.rules.domain

import aktual.budget.model.RuleId

// Saves and deletes rules, and syncs the change
interface RulesWriter {
  suspend fun save(rule: Rule)

  suspend fun delete(id: RuleId)
}
