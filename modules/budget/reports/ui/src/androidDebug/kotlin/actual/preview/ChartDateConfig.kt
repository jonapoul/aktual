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
package actual.preview

import actual.budget.reports.ui.charts.ChartDateConfig
import actual.budget.reports.vm.ChartDateConfig
import actual.budget.reports.vm.DateRangeMode
import actual.core.model.ColorSchemeType
import actual.core.ui.PreviewWithColorScheme
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.YearMonthRange

@Preview
@Composable
private fun PreviewLight() = PreviewConfig(ColorSchemeType.Light)

@Preview
@Composable
private fun PreviewDark() = PreviewConfig(ColorSchemeType.Dark)

@Preview
@Composable
private fun PreviewMidnight() = PreviewConfig(ColorSchemeType.Midnight)

@Composable
@Suppress("MagicNumber")
private fun PreviewConfig(type: ColorSchemeType) = PreviewWithColorScheme(type) {
  ChartDateConfig(
    modifier = Modifier.padding(8.dp),
    onNewConfig = {},
    onDateRangeType = {},
    config = ChartDateConfig(
      mode = DateRangeMode.Static,
      start = YearMonth(2025, Month.FEBRUARY),
      end = YearMonth(2025, Month.JULY),
      range = YearMonthRange(
        start = YearMonth(2011, Month.SEPTEMBER),
        endInclusive = YearMonth(2025, Month.JULY),
      ),
    ),
  )
}
