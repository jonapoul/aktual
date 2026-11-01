package aktual.budget.reports.components

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.reports.vm.NetWorthData
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.formattedText
import aktual.core.ui.verticalScrollWithBar
import alakazam.compose.HorizontalSpacer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minusMonth

@Immutable
internal data class NetWorthBreakdown(
  val month: YearMonth,
  // Top band first, like the chart reads. Empty in trend mode
  val rows: ImmutableList<NetWorthBreakdownRow>,
  val assets: Amount,
  val debts: Amount,
  val total: Amount,
  // Null for the first month, which has nothing to compare with
  val change: Amount?,
)

@Immutable
internal data class NetWorthBreakdownRow(
  val id: AccountId,
  val name: String,
  val index: Int,
  val amount: Amount,
)

// NetWorthGraph.tsx's tooltip: each account in stacked mode, then assets, debts, net worth and the
// change from the month before
internal fun netWorthBreakdown(data: NetWorthData, month: YearMonth): NetWorthBreakdown {
  val amounts = data.accounts.map { it.balances[month] ?: Zero }
  val rows =
    if (data.isStacked()) {
      data.accounts
        .mapIndexed { index, account ->
          NetWorthBreakdownRow(account.id, account.name, index, amounts[index])
        }
        .asReversed()
    } else {
      emptyList()
    }
  val total = data.items[month] ?: Zero
  return NetWorthBreakdown(
    month = month,
    rows = rows.toImmutableList(),
    assets = amounts.filter { it > Zero }.fold(Zero, Amount::plus),
    debts = amounts.filter { it < Zero }.fold(Zero, Amount::plus),
    total = total,
    change = data.items[month.minusMonth()]?.let { total - it },
  )
}

@Composable
internal fun NetWorthBreakdownList(
  data: NetWorthData,
  month: YearMonth,
  highlighted: AccountId?,
  onHighlight: (AccountId?) -> Unit,
  modifier: Modifier = Modifier,
) {
  val breakdown = remember(data, month) { netWorthBreakdown(data, month) }
  val monthStrings = monthStringsMap()
  Column(modifier = modifier.verticalScrollWithBar()) {
    Text(
      modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
      text = "${monthStrings[month.month]} ${month.year}",
      color = colors.pageText,
      style = typography.bodyLarge,
      fontWeight = Bold,
    )

    breakdown.rows.fastForEach { row ->
      val isDimmed = highlighted != null && highlighted != row.id
      BreakdownRow(
        modifier = Modifier.clickable { onHighlight(if (highlighted == row.id) null else row.id) },
        name = row.name,
        amount = row.amount,
        dot = colors.qualitative(row.index),
        alpha = if (isDimmed) DIMMED_ALPHA else 1f,
      )
    }

    if (breakdown.rows.isNotEmpty()) {
      HorizontalDivider(
        modifier = Modifier.padding(vertical = 4.dp),
        color = colors.tableBorder,
      )
    }

    BreakdownRow(name = Strings.reportsCustomAssets, amount = breakdown.assets)
    BreakdownRow(name = Strings.reportsCustomDebts, amount = breakdown.debts)
    BreakdownRow(name = Strings.reportsNetWorthTotal, amount = breakdown.total, isBold = true)
    breakdown.change?.let { change ->
      BreakdownRow(
        name = Strings.reportsNetWorthChange,
        amount = change,
        includeSign = true,
        amountColor = if (change.isPositive()) colors.noticeText else colors.errorText,
      )
    }
  }
}

@Composable
private fun BreakdownRow(
  name: String,
  amount: Amount,
  modifier: Modifier = Modifier,
  dot: Color? = null,
  alpha: Float = 1f,
  isBold: Boolean = false,
  includeSign: Boolean = false,
  amountColor: Color = colors.pageText,
) =
  Row(
    modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
    verticalAlignment = CenterVertically,
  ) {
    if (dot != null) {
      Box(modifier = Modifier.size(10.dp).background(dot.copy(alpha = alpha), CircleShape))
      HorizontalSpacer(8.dp)
    }

    Text(
      modifier = Modifier.weight(1f),
      text = name,
      color = colors.pageText.copy(alpha = alpha),
      style = typography.bodyMedium,
      fontWeight = if (isBold) Bold else null,
      maxLines = 1,
      overflow = Ellipsis,
    )

    HorizontalSpacer(8.dp)

    Text(
      text = amount.formattedText(includeSign = includeSign),
      color = amountColor.copy(alpha = alpha),
      style = typography.bodyMedium,
      fontWeight = if (isBold) Bold else null,
    )
  }

internal const val DIMMED_ALPHA = 0.3f
