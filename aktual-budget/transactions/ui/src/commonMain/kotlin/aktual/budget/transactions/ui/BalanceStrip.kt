package aktual.budget.transactions.ui

import aktual.budget.model.Amount
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ColoredParameters
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.formattedText
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val StripHeight = 40.dp

@Composable
internal fun BalanceStrip(balance: Amount?, modifier: Modifier = Modifier) =
  Column(modifier = modifier.fillMaxWidth().background(colors.tableBackground)) {
    Row(
      modifier = Modifier.fillMaxWidth().height(StripHeight).padding(horizontal = 16.dp),
      verticalAlignment = CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = Strings.transactionsHeaderBalance,
        fontSize = 13.sp,
        color = colors.pageTextLight,
      )

      Text(
        text = balance?.formattedText() ?: AnnotatedString(""),
        fontSize = 15.sp,
        fontWeight = SemiBold,
        color = colors.tableText,
        style = tabularFigures(),
        maxLines = 1,
      )
    }

    HorizontalDivider(color = colors.tableBorder)
  }

@Preview
@Composable
private fun PreviewBalanceStrip(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) { BalanceStrip(PREVIEW_BALANCE) }
