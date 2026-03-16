package aktual.budget.db.dao

import aktual.budget.db.converters.YearMonthFromLongConverter
import aktual.budget.db.model.ZeroBudget
import aktual.budget.model.CategoryId
import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.TypeConverters
import kotlinx.datetime.YearMonth

@Dao
@TypeConverters(YearMonthFromLongConverter::class)
interface ZeroBudgetsDao {
  @Query(
    """
    SELECT b.*
    FROM zero_budgets b
    LEFT JOIN categories c ON c.id = b.category
    WHERE c.tombstone = 0
    """
  )
  suspend fun getWithCategory(): List<ZeroBudget>

  @Query("SELECT carryover FROM zero_budgets WHERE month = :month AND category = :category")
  suspend fun getCarryover(month: YearMonth, category: CategoryId): Int?
}
