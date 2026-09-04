package aktual.budget.db.test

import aktual.budget.db.Accounts
import aktual.budget.db.CustomReports
import aktual.budget.model.AccountId
import aktual.budget.model.AccountSyncSource
import aktual.budget.model.Condition
import aktual.budget.model.CustomReportId
import aktual.budget.model.DateRangeType
import aktual.budget.model.ReportDate
import aktual.budget.model.SelectedCategory
import kotlin.uuid.Uuid
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.serialization.json.JsonObject

internal fun buildAccount(
  id: AccountId = AccountId("abc-123"),
  accountId: String = "xyz-789",
  name: String = "John Doe",
  officialName: String = "Jonathan Doe",
  bank: Uuid = BANK_ID,
  offBudget: Boolean = false,
  syncSource: AccountSyncSource = GoCardless,
) =
  Accounts(
    id = id,
    account_id = accountId,
    name = name,
    balance_current = null,
    balance_available = null,
    balance_limit = null,
    mask = null,
    official_name = officialName,
    subtype = null,
    bank = bank,
    offbudget = offBudget,
    closed = false,
    tombstone = false,
    sort_order = null,
    type = null,
    account_sync_source = syncSource,
    last_sync = null,
    last_reconciled = null,
    bank_sync_status = null,
    account_group_id = null,
  )

internal fun buildCustomReport(
  id: CustomReportId = CustomReportId("abc-123"),
  name: String? = "My report",
  startDate: ReportDate = ReportDate.Month(YearMonth(1999, Month.JANUARY)),
  endDate: ReportDate = ReportDate.Month(YearMonth(2025, Month.DECEMBER)),
  range: DateRangeType = Last12Months,
  selectedCategories: List<SelectedCategory> = emptyList(),
  conditions: List<Condition> = emptyList(),
  metadata: JsonObject? = null,
) =
  CustomReports(
    id = id,
    name = name,
    start_date = startDate,
    end_date = endDate,
    date_static = false,
    date_range = range,
    mode = Total,
    group_by = Category,
    balance_type = Expense,
    show_empty = false,
    show_offbudget = false,
    show_hidden = false,
    show_uncategorized = false,
    selected_categories = selectedCategories,
    graph_type = BarGraph,
    conditions = conditions,
    conditions_op = And,
    metadata = metadata,
    interval = Monthly,
    color_scheme = null,
    tombstone = false,
    include_current = false,
    sort_by = Desc,
    trim_intervals = false,
    show_trend_lines = false,
  )
