@file:Suppress("CompositionLocalAllowlist")

package actual.budget.transactions.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal val LocalTableDimens = compositionLocalOf<TableSpacings> { TableSpacings() }

internal data class TableSpacings(
  val interColumn: Dp = 5.dp,
  val rowVertical: Dp = 3.dp,
  val rowHorizontal: Dp = 3.dp,
  val textSize: TextUnit = 14.sp,
)
