package aktual.budget.db.dao

import aktual.budget.db.model.Rule
import aktual.budget.model.RuleId
import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert

@Dao
interface RulesDao {
  @Query(
    "SELECT * FROM rules WHERE conditions IS NOT NULL AND actions IS NOT NULL AND tombstone = 0"
  )
  suspend fun getWithConditionsAndActions(): List<Rule>

  @Upsert suspend fun insert(rule: Rule)

  @Query("DELETE FROM rules WHERE id = :id") suspend fun delete(id: RuleId)
}
