/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.core.ui.previews

import aktual.core.ui.PickDateDialogContent
import aktual.core.ui.PreviewThemedColumn
import aktual.core.ui.YearMonthPicker
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
