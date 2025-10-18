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
package actual.core.ui.previews

import actual.core.ui.PickDateDialogContent
import actual.core.ui.PreviewThemedColumn
import actual.core.ui.YearMonthPicker
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
private fun PreviewYearMonthPicker() = PreviewThemedColumn {
  YearMonthPicker(
    modifier = Modifier.padding(4.dp),
    value = YearMonth(2025, Month.FEBRUARY),
    onValueChange = {},
    range = YearMonthRange(
      start = YearMonth(2011, Month.DECEMBER),
      endInclusive = YearMonth(2025, Month.JULY),
    ),
  )
}

@Preview
@Composable
private fun PreviewDialogContent() = PreviewThemedColumn {
  PickDateDialogContent(
    value = YearMonth(2025, Month.FEBRUARY),
    range = YearMonthRange(
      start = YearMonth(2011, Month.DECEMBER),
      endInclusive = YearMonth(2025, Month.JULY),
    ),
    onDismiss = {},
    onValueChange = {},
  )
}

@Preview
@Composable
private fun PreviewDialogContentOutOfRange() = PreviewThemedColumn {
  PickDateDialogContent(
    value = YearMonth(2025, Month.AUGUST),
    range = YearMonthRange(
      start = YearMonth(2011, Month.DECEMBER),
      endInclusive = YearMonth(2025, Month.JULY),
    ),
    onDismiss = {},
    onValueChange = {},
  )
}
