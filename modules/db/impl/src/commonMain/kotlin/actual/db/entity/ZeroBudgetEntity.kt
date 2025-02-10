package actual.db.entity

import actual.budget.model.CategoryId
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "zero_budgets",
  foreignKeys = [
    ForeignKey(CategoryEntity::class, parentColumns = ["id"], childColumns = ["category"]),
  ],
  indices = [
    Index(value = ["category"]),
  ],
)
data class ZeroBudgetEntity(
  @PrimaryKey @ColumnInfo("id") val id: String,
  @ColumnInfo("month") val month: Int?,
  @ColumnInfo("category") val category: CategoryId?,
  @ColumnInfo("amount") val amount: Int? = 0,
  @ColumnInfo("carryover") val carryover: Int? = 0,
  @ColumnInfo("goal") val goal: Int? = null,
  @ColumnInfo("long_goal") val longGoal: Int? = null,
)
