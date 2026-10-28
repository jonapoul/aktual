package aktual.budget.transactions.ui

import aktual.budget.db.dao.ClearedBalances
import aktual.budget.model.Amount
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.textSizes
import aktual.core.ui.ColorParameters
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

private val StripHeight = 40.dp

private val ExtraHeight = 28.dp

// With the account's cleared and uncleared totals underneath, if they're shown
@Composable
internal fun BalanceStrip(
  balance: Amount?,
  modifier: Modifier = Modifier,
  clearedBalances: ClearedBalances? = null,
) =
  Column(modifier = modifier.fillMaxWidth().background(colors.tableBackground)) {
    Row(
      modifier = Modifier.fillMaxWidth().height(StripHeight).padding(horizontal = 16.dp),
      verticalAlignment = CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = Strings.transactionsHeaderBalance,
        fontSize = textSizes.smallPlus,
        color = colors.pageTextLight,
      )

      Text(
        text = balance?.formattedText() ?: AnnotatedString(""),
        fontSize = textSizes.mediumPlus,
        fontWeight = SemiBold,
        color = colors.tableText,
        style = tabularFigures(),
        maxLines = 1,
      )
    }

    if (clearedBalances != null) {
      Row(
        modifier =
          Modifier.fillMaxWidth()
            .height(ExtraHeight)
            .padding(horizontal = 16.dp)
            .padding(bottom = 4.dp),
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        ExtraBalance(Strings.transactionsHeaderCleared, Amount(clearedBalances.cleared))
        ExtraBalance(Strings.transactionsHeaderUncleared, Amount(clearedBalances.uncleared))
      }
    }

    HorizontalDivider(color = colors.tableBorder)
  }

@Composable
private fun ExtraBalance(label: String, amount: Amount) =
  Row(
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    Text(text = label, fontSize = textSizes.small, color = colors.pageTextLight)
    Text(
      text = amount.formattedText(),
      fontSize = textSizes.small,
      fontWeight = Medium,
      color = colors.tableText,
      style = tabularFigures(),
      maxLines = 1,
    )
  }

@Preview
@Composable
private fun PreviewBalanceStrip(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) { BalanceStrip(PREVIEW_BALANCE) }

@Preview
@Composable
private fun PreviewBalanceStripWithExtras(
  @PreviewParameter(ColorParameters::class) colors: Colors,
) =
  PreviewWithColors(colors) {
    BalanceStrip(PREVIEW_BALANCE, clearedBalances = PREVIEW_CLEARED_BALANCES)
  }
