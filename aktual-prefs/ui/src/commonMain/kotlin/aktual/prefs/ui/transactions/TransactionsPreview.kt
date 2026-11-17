package aktual.prefs.ui.transactions

import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.CardShape
import aktual.core.ui.ColorParameters
import aktual.core.ui.PreviewWithColors
import aktual.prefs.ui.SampleTransactionList
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

@Composable
internal fun TransactionsPreview(
  alternateRowColours: Boolean,
  expandSplits: Boolean,
  modifier: Modifier = Modifier,
) =
  SampleTransactionList(
    modifier =
      modifier
        .fillMaxWidth()
        .height(PreviewHeight)
        .clip(CardShape)
        .background(colors.tableBackground),
    alternateRowColours = alternateRowColours,
    expandSplits = expandSplits,
  )

private val PreviewHeight = 260.dp

@Preview
@Composable
private fun PreviewTransactionsPreview(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    TransactionsPreview(alternateRowColours = true, expandSplits = true)
  }
