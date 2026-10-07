package aktual.budget.transactions.ui

import aktual.budget.model.Amount
import aktual.budget.model.TransactionsDensity
import aktual.budget.transactions.vm.Transaction
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.formattedString
import aktual.core.ui.stringShort
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

// Comfortable and Compact
@Composable
internal fun LedgerRow(
  transaction: Transaction,
  showDate: Boolean,
  modifier: Modifier = Modifier,
) {
  val dimens = LocalLedgerDimens.current
  Row(
    modifier = modifier.fillMaxWidth().heightIn(min = dimens.rowHeight),
    verticalAlignment = CenterVertically,
  ) {
    Box(
      modifier =
        Modifier.align(Alignment.Top).width(dimens.railWidth).padding(top = dimens.railTop),
      contentAlignment = TopCenter,
    ) {
      if (showDate) DateRail(transaction.date, dimens)
    }

    Column(modifier = Modifier.weight(1f).padding(vertical = dimens.rowVertical)) {
      Text(
        text = transaction.payee.orEmpty(),
        fontSize = dimens.payeeSize,
        fontWeight = dimens.payeeWeight,
        color = colors.pageTextDark,
        overflow = Ellipsis,
        maxLines = 1,
      )

      SecondLine(transaction, dimens)
    }

    Column(
      modifier = Modifier.padding(start = dimens.contentGap, end = dimens.rowEnd),
      horizontalAlignment = Alignment.End,
    ) {
      AmountText(transaction.amount, dimens)
      if (dimens.showBalance) BalanceText(transaction.balance, dimens)
    }
  }
}

@Composable
private fun DateRail(date: LocalDate, dimens: LedgerDimens) =
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = date.day.toString(),
      fontSize = dimens.dayNumberSize,
      lineHeight = dimens.dayNumberSize * DAY_NUMBER_LINE_HEIGHT,
      fontWeight = Bold,
      color = colors.pageTextDark,
      maxLines = 1,
    )

    Text(
      text = date.month.stringShort().take(LABEL_LENGTH).uppercase(),
      fontSize = dimens.weekdaySize,
      fontWeight = SemiBold,
      color = colors.pageTextLight,
      maxLines = 1,
    )
  }

@Composable
private fun SecondLine(transaction: Transaction, dimens: LedgerDimens) {
  val needsCategory = Strings.transactionsNeedsCategory
  val warning = SpanStyle(color = colors.warningText, fontWeight = SemiBold)
  val text = buildAnnotatedString {
    val category = transaction.category
    if (transaction.needsCategory) {
      withStyle(warning) { append(needsCategory) }
    } else if (category != null) {
      append(category)
    }

    val account = transaction.account
    if (dimens.showAccount && account != null) {
      if (length > 0) append(" · ")
      append(account)
    }
  }

  Text(
    text = text,
    fontSize = dimens.secondLineSize,
    color = colors.pageTextLight,
    overflow = Ellipsis,
    maxLines = 1,
  )
}

// Dense
@Composable
internal fun LedgerTableRow(transaction: Transaction, modifier: Modifier = Modifier) {
  val dimens = LocalLedgerDimens.current
  Row(
    modifier = modifier.fillMaxWidth().height(dimens.rowHeight).padding(horizontal = dimens.rowEnd),
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(dimens.contentGap),
  ) {
    Text(
      modifier = Modifier.width(DenseColumns.date),
      text = transaction.date.dayAndMonth(),
      fontSize = dimens.secondLineSize,
      color = colors.pageTextLight,
      style = tabularFigures(),
      maxLines = 1,
    )

    Text(
      modifier = Modifier.weight(DenseColumns.PAYEE_WEIGHT),
      text = transaction.payee.orEmpty(),
      fontSize = dimens.payeeSize,
      fontWeight = dimens.payeeWeight,
      color = colors.pageTextDark,
      overflow = Ellipsis,
      maxLines = 1,
    )

    val needsCategory = transaction.needsCategory
    Text(
      modifier = Modifier.weight(DenseColumns.CATEGORY_WEIGHT),
      text = if (needsCategory) Strings.transactionsNoCategory else transaction.category.orEmpty(),
      fontSize = dimens.secondLineSize,
      fontWeight = if (needsCategory) SemiBold else null,
      color = if (needsCategory) colors.warningText else colors.pageTextLight,
      overflow = Ellipsis,
      maxLines = 1,
    )

    AmountText(transaction.amount, dimens, Modifier.width(DenseColumns.amount))
    if (dimens.showBalance) {
      BalanceText(transaction.balance, dimens, Modifier.width(DenseColumns.balance))
    }
  }
}

@Composable
private fun AmountText(amount: Amount, dimens: LedgerDimens, modifier: Modifier = Modifier) =
  Text(
    modifier = modifier,
    text = amount.formattedString(includeSign = true),
    fontSize = dimens.amountSize,
    fontWeight = dimens.amountWeight,
    color = amount.color(),
    textAlign = End,
    style = tabularFigures(),
    overflow = Ellipsis,
    maxLines = 1,
  )

@Composable
private fun BalanceText(balance: Amount?, dimens: LedgerDimens, modifier: Modifier = Modifier) =
  Text(
    modifier = modifier,
    text = balance?.formattedString().orEmpty(),
    fontSize = dimens.balanceSize,
    color = colors.pageTextSubdued,
    textAlign = End,
    style = tabularFigures(),
    overflow = Ellipsis,
    maxLines = 1,
  )

@Composable
@ReadOnlyComposable
private fun Amount.color(): Color = if (this > Zero) colors.numberPositive else colors.tableText

@Composable
@ReadOnlyComposable
internal fun tabularFigures(style: TextStyle = LocalTextStyle.current): TextStyle =
  style.copy(fontFeatureSettings = "tnum")

private fun LocalDate.dayAndMonth(): String {
  val day = day.toString().padStart(length = 2, padChar = '0')
  val month = month.number.toString().padStart(length = 2, padChar = '0')
  return "$day/$month"
}

@Composable
internal fun LedgerShimmerRow(modifier: Modifier = Modifier) {
  val dimens = LocalLedgerDimens.current
  val barHeight = dimens.rowHeight / SHIMMER_BAR_FRACTION
  val bar = Modifier.height(barHeight).background(colors.tableText, CardShape)

  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .height(dimens.rowHeight)
        .padding(horizontal = dimens.rowEnd)
        .shimmer(rememberShimmer(Window)),
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(dimens.contentGap),
  ) {
    Box(modifier = bar.width(dimens.railWidth - dimens.rowEnd))
    Box(modifier = bar.weight(1f))
    Box(modifier = bar.width(DenseColumns.amount))
  }
}

private const val DAY_NUMBER_LINE_HEIGHT = 1.1f
private const val LABEL_LENGTH = 3
private const val SHIMMER_BAR_FRACTION = 1.5f

@Preview
@Composable
private fun PreviewLedgerRow(
  @PreviewParameter(LedgerRowProvider::class) params: ColoredParams<LedgerRowParams>
) =
  PreviewWithColoredParams(params) {
    WithLedgerDimens(density) {
      when (density) {
        Comfortable,
        Compact -> LedgerRow(transaction, showDate = showDate)
        Dense -> LedgerTableRow(transaction)
      }
    }
  }

@Preview
@Composable
private fun PreviewLedgerShimmerRow(
  @PreviewParameter(DensityProvider::class) params: ColoredParams<TransactionsDensity>
) = PreviewWithColoredParams(params) { WithLedgerDimens(this) { LedgerShimmerRow() } }

private data class LedgerRowParams(
  val density: TransactionsDensity,
  val transaction: Transaction,
  val showDate: Boolean = true,
)

private class LedgerRowProvider :
  ColoredParameterProvider<LedgerRowParams>(
    LedgerRowParams(Comfortable, TRANSACTION_1),
    LedgerRowParams(Comfortable, TRANSACTION_2, showDate = false),
    LedgerRowParams(Comfortable, TRANSACTION_3),
    LedgerRowParams(Comfortable, TRANSACTION_UNCATEGORISED),
    LedgerRowParams(Compact, TRANSACTION_1),
    LedgerRowParams(Compact, TRANSACTION_2, showDate = false),
    LedgerRowParams(Compact, TRANSACTION_3),
    LedgerRowParams(Compact, TRANSACTION_UNCATEGORISED),
    LedgerRowParams(Dense, TRANSACTION_1),
    LedgerRowParams(Dense, TRANSACTION_3),
    LedgerRowParams(Dense, TRANSACTION_UNCATEGORISED),
  )
