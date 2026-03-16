package aktual.budget.db.model

import aktual.budget.model.CategoryId
import androidx.room3.ColumnInfo

data class CategoryWithTemplateNote(
  @ColumnInfo(name = "id") val id: CategoryId,
  @ColumnInfo(name = "name") val name: String?,
  @ColumnInfo(name = "note") val note: String?,
)
