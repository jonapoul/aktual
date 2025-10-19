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
package aktual.preview

import aktual.budget.reports.ui.charts.NetWorthChart
import aktual.budget.reports.ui.charts.PreviewNetWorth
import aktual.budget.reports.ui.charts.PreviewShared.WIDTH
import aktual.core.ui.CardShape
import aktual.core.ui.LocalTheme
import aktual.core.ui.PreviewThemedColumn
import aktual.core.ui.PreviewThemedScreen
import aktual.core.ui.TripleScreenPreview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@TripleScreenPreview
@Composable
private fun MixedRegular() = PreviewThemedScreen {
  NetWorthChart(
    data = PreviewNetWorth.DATA,
    compact = false,
  )
}

@TripleScreenPreview
@Composable
private fun MixedRegularPrivate() = PreviewThemedScreen(isPrivacyEnabled = true) {
  NetWorthChart(
    data = PreviewNetWorth.DATA,
    compact = false,
  )
}

@Preview(widthDp = WIDTH)
@Composable
private fun PreviewCompact() = PreviewThemedColumn(isPrivacyEnabled = false) {
  NetWorthChart(
    modifier = Modifier
      .background(LocalTheme.current.tableBackground, CardShape)
      .width(WIDTH.dp),
    data = PreviewNetWorth.DATA,
    compact = true,
  )
}

@Preview(widthDp = WIDTH)
@Composable
private fun PreviewCompactPrivate() = PreviewThemedColumn(isPrivacyEnabled = true) {
  NetWorthChart(
    modifier = Modifier
      .background(LocalTheme.current.tableBackground, CardShape)
      .width(WIDTH.dp),
    data = PreviewNetWorth.DATA,
    compact = true,
  )
}
