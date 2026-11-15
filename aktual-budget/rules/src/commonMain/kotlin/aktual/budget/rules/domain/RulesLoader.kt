package aktual.budget.rules.domain

import aktual.budget.model.RuleId
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

// Every rule, in the order they run
interface RulesLoader {
  suspend fun load(): ImmutableList<Rule>

  suspend fun load(id: RuleId): Rule?

  fun observe(): Flow<ImmutableList<Rule>>
}
