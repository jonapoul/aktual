package aktual.budget.transactions.ui

import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ColoredParameters
import aktual.core.ui.PreviewWithColors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val HeaderHeight = 26.dp

@Composable
internal fun LedgerHeader(modifier: Modifier = Modifier) {
  val dimens = LocalLedgerDimens.current
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .height(HeaderHeight)
        .background(colors.tableHeaderBackground)
        .padding(horizontal = dimens.rowEnd),
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(dimens.contentGap),
  ) {
    LedgerHeaderText(Strings.transactionsHeaderDate, Modifier.width(DenseColumns.date))
    LedgerHeaderText(
      Strings.transactionsHeaderPayee,
      Modifier.weight(DenseColumns.PAYEE_WEIGHT),
    )
    LedgerHeaderText(
      Strings.transactionsHeaderCategory,
      Modifier.weight(DenseColumns.CATEGORY_WEIGHT),
    )
    LedgerHeaderText(
      Strings.transactionsHeaderAmount,
      Modifier.width(DenseColumns.amount),
      textAlign = End,
    )
    LedgerHeaderText(
      Strings.transactionsHeaderBalance,
      Modifier.width(DenseColumns.balance),
      textAlign = End,
    )
  }
}

@Composable
private fun LedgerHeaderText(
  text: String,
  modifier: Modifier = Modifier,
  textAlign: TextAlign = Start,
) =
  Text(
    modifier = modifier,
    text = text.uppercase(),
    textAlign = textAlign,
    fontSize = 11.sp,
    fontWeight = SemiBold,
    color = colors.tableHeaderText,
    overflow = Ellipsis,
    maxLines = 1,
  )

@Preview
@Composable
private fun PreviewLedgerHeader(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) { WithLedgerDimens(Dense) { LedgerHeader() } }
