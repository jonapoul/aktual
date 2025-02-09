package actual.db.entity

import actual.db.model.Condition
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_reports")
data class CustomReportEntity(
  @PrimaryKey @ColumnInfo("id") val id: String,
  @ColumnInfo("name") val name: String?,
  @ColumnInfo("start_date") val startDate: String?,
  @ColumnInfo("end_date") val endDate: String?,
  @ColumnInfo("date_static") val dateStatic: Boolean? = false,
  @ColumnInfo("date_range") val dateRange: String?,
  @ColumnInfo("mode") val mode: String? = "total",
  @ColumnInfo("group_by") val groupBy: String? = "Category",
  @ColumnInfo("balance_type") val balanceType: String? = "Expense",
  @ColumnInfo("show_empty") val showEmpty: Boolean? = false,
  @ColumnInfo("show_offbudget") val showOffBudget: Boolean? = false,
  @ColumnInfo("show_hidden") val showHidden: Boolean? = false,
  @ColumnInfo("show_uncategorized") val showUncategorized: Boolean? = false,
  @ColumnInfo("selected_categories") val selectedCategories: String?,
  @ColumnInfo("graph_type") val graphType: String? = "BarGraph",
  @ColumnInfo("conditions") val conditions: String?,
  @ColumnInfo("conditions_op") val conditionsOp: Condition? = Condition.And,
  @ColumnInfo("metadata") val metadata: String?,
  @ColumnInfo("interval") val interval: String? = "Monthly",
  @ColumnInfo("color_scheme") val colorScheme: String?,
  @ColumnInfo("tombstone") val tombstone: Boolean? = false,
  @ColumnInfo("include_current") val includeCurrent: Boolean? = false,
  @ColumnInfo("sort_by") val sortBy: String? = "desc",
)
