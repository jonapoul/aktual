package aktual.budget.rules.domain

import aktual.budget.db.DbJson
import aktual.budget.db.RuleJson
import aktual.budget.db.Rules
import aktual.budget.db.dao.DatabaseTables.RULES
import aktual.budget.db.dao.RulesDao
import aktual.budget.model.LocalChange
import aktual.budget.model.RuleId
import aktual.budget.model.localChange
import aktual.budget.model.tombstone
import aktual.budget.utils.BudgetSyncController
import aktual.di.BudgetScope
import aktual.log
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(BudgetScope::class)
class RulesWriterImpl(
  private val rulesDao: RulesDao,
  private val syncController: BudgetSyncController,
) : RulesWriter {
  override suspend fun save(rule: Rule) {
    val row =
      with(rule) {
        Rules(
          id = id,
          stage = stage,
          conditions = conditions,
          actions = actions,
          tombstone = false,
          conditions_op = conditionsOp,
        )
      }
    rulesDao.insert(row)
    log.i { "Saved $row" }
    syncController.syncChanges(insertChanges(row))
  }

  override suspend fun delete(id: RuleId) {
    rulesDao.tombstone(setOf(id))
    syncController.syncChanges(tombstone(dataset = RULES, row = id.toString()))
    log.d { "Tombstoned rule $id" }
  }

  private fun insertChanges(rule: Rules): List<LocalChange> {
    val row = rule.id.toString()
    return with(rule) {
      listOf(
        localChange(RULES, row, "id", row),
        localChange(RULES, row, "stage", stage),
        localChange(
          RULES,
          row,
          "conditions",
          DbJson.encodeToString(RuleJson.conditions, conditions.orEmpty()),
        ),
        localChange(
          RULES,
          row,
          "actions",
          DbJson.encodeToString(RuleJson.actions, actions.orEmpty()),
        ),
        localChange(RULES, row, "tombstone", tombstone),
        localChange(RULES, row, "conditions_op", conditions_op),
      )
    }
  }
}
