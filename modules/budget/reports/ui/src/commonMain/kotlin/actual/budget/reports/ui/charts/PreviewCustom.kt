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
package actual.budget.reports.ui.charts

import actual.budget.reports.vm.CustomData
import actual.budget.reports.vm.DateRangeMode
import actual.budget.reports.vm.ReportTimeRange
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.YearMonthRange

internal object PreviewCustom {
  val DATA = CustomData(
    title = "My Custom Report",
    mode = DateRangeMode.Live,
    range = ReportTimeRange.Specific(
      range = YearMonthRange(
        start = YearMonth(2011, Month.SEPTEMBER),
        endInclusive = YearMonth(2025, Month.JULY),
      ),
    ),
  )
}
