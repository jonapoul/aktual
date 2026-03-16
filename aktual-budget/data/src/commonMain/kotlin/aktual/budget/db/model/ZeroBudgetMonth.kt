package aktual.budget.db.model

import aktual.budget.model.ZeroBudgetMonthId
import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "zero_budget_months")
data class ZeroBudgetMonth(
  @PrimaryKey @ColumnInfo(name = "id") val id: ZeroBudgetMonthId,
  @ColumnInfo(name = "buffered") val buffered: Boolean? = false,
)
