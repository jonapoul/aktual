package actual.db.entity

import actual.budget.model.ZeroBudgetMonthId
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "zero_budget_months")
data class ZeroBudgetMonthEntity(
  @PrimaryKey @ColumnInfo("id") val id: ZeroBudgetMonthId,
  @ColumnInfo("buffered") val isBuffered: Boolean? = false,
)
