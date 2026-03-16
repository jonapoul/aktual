package aktual.budget.db.test

import aktual.budget.db.model.Account
import aktual.budget.db.model.CustomReport
import aktual.budget.model.AccountId
import aktual.budget.model.AccountSyncSource
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
  syncSource: AccountSyncSource = AccountSyncSource.GoCardless,
) =
  Account(
    id = id,
    accountId = accountId,
    name = name,
    balanceCurrent = null,
    balanceAvailable = null,
    balanceLimit = null,
    mask = null,
    officialName = officialName,
    subtype = null,
    bank = bank,
    offBudget = offBudget,
    closed = false,
    tombstone = false,
    sortOrder = null,
    type = null,
    accountSyncSource = syncSource,
    lastSync = null,
    lastReconciled = null,
  )

internal fun buildCustomReport(
  id: CustomReportId = CustomReportId("abc-123"),
  name: String? = "My report",
  startDate: ReportDate = ReportDate.Month(YearMonth(1999, Month.JANUARY)),
  endDate: ReportDate = ReportDate.Month(YearMonth(2025, Month.DECEMBER)),
  range: DateRangeType = DateRangeType.Last12Months,
  selectedCategories: List<SelectedCategory> = emptyList(),
  conditions: List<ReportCondition> = emptyList(),
  metadata: JsonObject? = null,
) =
  CustomReport(
    id = id,
    name = name,
    startDate = startDate,
    endDate = endDate,
    dateStatic = false,
    dateRange = range,
    mode = CustomReportMode.Total,
    groupBy = GroupBy.Category,
    balanceType = BalanceType.Expense,
    showEmpty = false,
    showOffbudget = false,
    showHidden = false,
    showUncategorized = false,
    selectedCategories = selectedCategories,
    graphType = GraphType.BarGraph,
    conditions = conditions,
    conditionsOp = Operator.And,
    metadata = metadata,
    interval = Interval.Monthly,
    colorScheme = null,
    tombstone = false,
    includeCurrent = false,
    sortBy = SortBy.Desc,
  )
