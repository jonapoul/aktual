package aktual.budget.db.model

import aktual.budget.model.BalanceType
import aktual.budget.model.CustomReportId
import aktual.budget.model.CustomReportMode
import aktual.budget.model.DateRangeType
import aktual.budget.model.GraphType
import aktual.budget.model.GroupBy
import aktual.budget.model.Interval
import aktual.budget.model.Operator
import aktual.budget.model.ReportCondition
import aktual.budget.model.ReportDate
import aktual.budget.model.SelectedCategory
import aktual.budget.model.SortBy
import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.json.JsonObject

@Entity(tableName = "custom_reports")
data class CustomReport(
  @PrimaryKey @ColumnInfo(name = "id") val id: CustomReportId,
  @ColumnInfo(name = "name") val name: String?,
  @ColumnInfo(name = "start_date") val startDate: ReportDate?,
  @ColumnInfo(name = "end_date") val endDate: ReportDate?,
  @ColumnInfo(name = "date_static") val dateStatic: Boolean? = false,
  @ColumnInfo(name = "date_range") val dateRange: DateRangeType?,
  @ColumnInfo(name = "mode") val mode: CustomReportMode?,
  @ColumnInfo(name = "group_by") val groupBy: GroupBy?,
  @ColumnInfo(name = "balance_type") val balanceType: BalanceType?,
  @ColumnInfo(name = "show_empty") val showEmpty: Boolean? = false,
  @ColumnInfo(name = "show_offbudget") val showOffbudget: Boolean? = false,
  @ColumnInfo(name = "show_hidden") val showHidden: Boolean? = false,
  @ColumnInfo(name = "show_uncategorized") val showUncategorized: Boolean? = false,
  @ColumnInfo(name = "selected_categories") val selectedCategories: List<SelectedCategory>?,
  @ColumnInfo(name = "graph_type") val graphType: GraphType?,
  @ColumnInfo(name = "conditions") val conditions: List<ReportCondition>?,
  @ColumnInfo(name = "conditions_op") val conditionsOp: Operator?,
  @ColumnInfo(name = "metadata") val metadata: JsonObject?,
  @ColumnInfo(name = "interval") val interval: Interval?,
  @ColumnInfo(name = "color_scheme") val colorScheme: String?,
  @ColumnInfo(name = "tombstone") val tombstone: Boolean? = false,
  @ColumnInfo(name = "include_current") val includeCurrent: Boolean? = false,
  @ColumnInfo(name = "sort_by") val sortBy: SortBy?,
)
