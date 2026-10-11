package aktual.budget.reports.vm

import aktual.budget.db.CustomReports
import aktual.budget.model.CustomReportId

// Every nullable column left empty, like a row written by an older or buggy client
internal fun emptyCustomReport(id: CustomReportId = CustomReportId("report-1")) =
  CustomReports(
    id = id,
    name = null,
    start_date = null,
    end_date = null,
    date_static = null,
    date_range = null,
    mode = null,
    group_by = null,
    balance_type = null,
    show_empty = null,
    show_offbudget = null,
    show_hidden = null,
    show_uncategorized = null,
    selected_categories = null,
    graph_type = null,
    conditions = null,
    conditions_op = null,
    metadata = null,
    interval = null,
    color_scheme = null,
    tombstone = null,
    include_current = null,
    sort_by = null,
    trim_intervals = null,
    show_trend_lines = null,
  )
