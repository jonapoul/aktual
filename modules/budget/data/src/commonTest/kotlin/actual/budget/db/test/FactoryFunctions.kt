/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package actual.budget.db.test

import actual.budget.db.Accounts
import actual.budget.db.CustomReports
import actual.budget.model.AccountId
import actual.budget.model.AccountSyncSource
import actual.budget.model.BalanceType
import actual.budget.model.CustomReportId
import actual.budget.model.CustomReportMode
import actual.budget.model.DateRangeType
import actual.budget.model.GraphType
import actual.budget.model.GroupBy
import actual.budget.model.Interval
import actual.budget.model.Operator
import actual.budget.model.ReportCondition
import actual.budget.model.ReportDate
import actual.budget.model.SelectedCategory
import actual.budget.model.SortBy
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.serialization.json.JsonObject
import kotlin.uuid.Uuid

internal fun buildAccount(
  id: AccountId = AccountId("abc-123"),
  accountId: String = "xyz-789",
  name: String = "John Doe",
  officialName: String = "Jonathan Doe",
  bank: Uuid = BANK_ID,
  offBudget: Boolean = false,
  syncSource: AccountSyncSource = AccountSyncSource.GoCardless,
) = Accounts(
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
) = CustomReports(
  id = id,
  name = name,
  start_date = startDate,
  end_date = endDate,
  date_static = false,
  date_range = range,
  mode = CustomReportMode.Total,
  group_by = GroupBy.Category,
  balance_type = BalanceType.Expense,
  show_empty = false,
  show_offbudget = false,
  show_hidden = false,
  show_uncategorized = false,
  selected_categories = selectedCategories,
  graph_type = GraphType.BarGraph,
  conditions = conditions,
  conditions_op = Operator.And,
  metadata = metadata,
  interval = Interval.Monthly,
  color_scheme = null,
  tombstone = false,
  include_current = false,
  sort_by = SortBy.Desc,
)
