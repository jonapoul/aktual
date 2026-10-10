package aktual.budget.transactions.ui

import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ColoredParameters
import aktual.core.ui.PreviewWithColors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DividerHeight = 24.dp

@Composable
internal fun YearDivider(year: Int, modifier: Modifier = Modifier) =
  Box(
    modifier =
      modifier
        .fillMaxWidth()
        .height(DividerHeight)
        .background(colors.tableHeaderBackground)
        .padding(horizontal = LocalLedgerDimens.current.rowEnd),
    contentAlignment = CenterStart,
  ) {
    Text(
      text = year.toString(),
      fontSize = 11.sp,
      fontWeight = SemiBold,
      color = colors.tableHeaderText,
      style = tabularFigures(),
      maxLines = 1,
    )
  }

@Preview
@Composable
private fun PreviewYearDivider(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) { YearDivider(year = 2026) }
