package aktual.budget.transactions.ui

import aktual.budget.model.Amount
import aktual.budget.model.TransactionsDensity
import aktual.budget.transactions.vm.Transaction
import aktual.core.icons.AktualIcons
import aktual.core.icons.Split
import aktual.core.icons.material.ExpandMore
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Plurals
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
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
  parts: SplitParts = Collapsed,
  onToggleSplit: () -> Unit = {},
) {
  val dimens = LocalLedgerDimens.current
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .heightIn(min = dimens.rowHeight)
        .then(splitToggle(transaction, parts, onToggleSplit)),
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
      PayeeText(transaction, dimens)
      SecondLine(transaction, dimens, parts)
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
private fun SecondLine(transaction: Transaction, dimens: LedgerDimens, parts: SplitParts) =
  CategoryLine(
    transaction = transaction,
    parts = parts,
    text =
      categoryText(
        transaction = transaction,
        parts = parts,
        needsCategory = Strings.transactionsNeedsCategory,
        account = transaction.account.takeIf { dimens.showAccount },
      ),
    dimens = dimens,
  )

// The category, or what stands in for it, with the split marker before it
@Composable
private fun CategoryLine(
  transaction: Transaction,
  parts: SplitParts,
  text: AnnotatedString,
  dimens: LedgerDimens,
  modifier: Modifier = Modifier,
) =
  Row(
    modifier = modifier,
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(SplitIconGap),
  ) {
    if (transaction.split != None) {
      Icon(
        modifier = Modifier.size(SplitIconSize),
        imageVector = AktualIcons.Split,
        contentDescription = null,
        tint = colors.pageTextLight,
      )
    }

    Text(
      modifier = Modifier.weight(1f, fill = false),
      text = text,
      fontSize = dimens.secondLineSize,
      color = colors.pageTextLight,
      overflow = Ellipsis,
      maxLines = 1,
    )

    if (transaction.isExpandable(parts)) {
      Icon(
        modifier = Modifier.size(ChevronSize).rotate(if (parts == Expanded) HALF_TURN else 0f),
        imageVector = MaterialIcons.ExpandMore,
        contentDescription = null,
        tint = colors.pageTextLight,
      )
    }
  }

@Composable
private fun categoryText(
  transaction: Transaction,
  parts: SplitParts,
  needsCategory: String,
  account: String? = null,
): AnnotatedString {
  val split = Strings.transactionsSplit
  val shown = transaction.children.size
  val total = transaction.totalChildren
  val shownParts =
    if (parts == Pinned && shown > 0) Plurals.transactionsSplitParts(total, shown, total) else null
  val warning = SpanStyle(color = colors.warningText, fontWeight = SemiBold)
  val special =
    when (transaction.specialCategory) {
      OffBudget -> Strings.transactionsOffBudget
      Transfer -> Strings.transactionsTransfer
      null -> null
    }

  return buildAnnotatedString {
    val category = transaction.category
    when {
      transaction.split == Parent -> {
        withStyle(SpanStyle(fontStyle = Italic)) { append(split) }
        if (shownParts != null) append(" · $shownParts")
      }

      // Wins over a real category, as upstream's prettyCategory does
      special != null -> {
        withStyle(SpanStyle(fontStyle = Italic)) { append(special) }
      }

      transaction.needsCategory -> {
        withStyle(warning) { append(needsCategory) }
      }

      category != null -> {
        append(category)
      }
    }

    if (account != null) {
      if (length > 0) append(" · ")
      append(account)
    }
  }
}

@Composable
private fun PayeeText(
  transaction: Transaction,
  dimens: LedgerDimens,
  modifier: Modifier = Modifier,
) {
  val noPayee = transaction.split == Parent && transaction.payee == null
  Text(
    modifier = modifier,
    text = if (noPayee) Strings.transactionsSplitNoPayee else transaction.payee.orEmpty(),
    fontSize = dimens.payeeSize,
    fontWeight = dimens.payeeWeight,
    fontStyle = if (noPayee) Italic else null,
    color = if (noPayee) colors.pageTextLight else colors.pageTextDark,
    overflow = Ellipsis,
    maxLines = 1,
  )
}

private fun Transaction.isExpandable(parts: SplitParts) =
  split == Parent && parts != Pinned && children.isNotEmpty()

// Tapping a split opens and closes its parts. Other rows stay inert
@Composable
private fun splitToggle(
  transaction: Transaction,
  parts: SplitParts,
  onToggleSplit: () -> Unit,
): Modifier {
  if (!transaction.isExpandable(parts)) return Modifier
  val expanded = parts == Expanded
  val state =
    if (expanded) Strings.transactionsSplitExpanded else Strings.transactionsSplitCollapsed
  return Modifier.toggleable(value = expanded, onValueChange = { onToggleSplit() }).semantics {
    stateDescription = state
  }
}

// Dense
@Composable
internal fun LedgerTableRow(
  transaction: Transaction,
  modifier: Modifier = Modifier,
  parts: SplitParts = Collapsed,
  onToggleSplit: () -> Unit = {},
) {
  val dimens = LocalLedgerDimens.current
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .height(dimens.rowHeight)
        .then(splitToggle(transaction, parts, onToggleSplit))
        .padding(horizontal = dimens.rowEnd),
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

    PayeeText(transaction, dimens, Modifier.weight(DenseColumns.PAYEE_WEIGHT))

    CategoryLine(
      modifier = Modifier.weight(DenseColumns.CATEGORY_WEIGHT),
      transaction = transaction,
      parts = parts,
      text = categoryText(transaction, parts, needsCategory = Strings.transactionsNoCategory),
      dimens = dimens,
    )

    AmountText(transaction.amount, dimens, Modifier.width(DenseColumns.amount))
    if (dimens.showBalance) {
      BalanceText(transaction.balance, dimens, Modifier.width(DenseColumns.balance))
    }
  }
}

// The parts of a split, on an inset under their parent's row
@Composable
internal fun SplitChildren(
  parent: Transaction,
  density: TransactionsDensity,
  modifier: Modifier = Modifier,
) {
  val dimens = LocalLedgerDimens.current
  when (density) {
    // The inset runs from the payee column to the screen edge, so amounts line up with the parent's
    Comfortable,
    Compact -> {
      Column(
        modifier =
          modifier
            .fillMaxWidth()
            .padding(start = dimens.railWidth, bottom = dimens.rowVertical)
            .background(colors.pageBackgroundModalActive)
            .padding(vertical = SplitInsetVertical),
      ) {
        parent.children.fastForEach { child -> SplitChildRow(child, parent, dimens) }
      }
    }

    Dense -> {
      Column(modifier = modifier.fillMaxWidth().background(colors.pageBackgroundModalActive)) {
        parent.children.fastForEach { child -> SplitChildTableRow(child, parent, dimens) }
      }
    }
  }
}

@Composable
private fun SplitChildRow(child: Transaction, parent: Transaction, dimens: LedgerDimens) =
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .height(dimens.childRowHeight)
        .padding(start = SplitInsetStart, end = dimens.rowEnd),
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(dimens.contentGap),
  ) {
    val category =
      categoryText(child, parts = Collapsed, needsCategory = Strings.transactionsNeedsCategory)
    val payee = child.payeeUnlike(parent)
    val payeeStyle = SpanStyle(color = colors.tableText)

    Text(
      modifier = Modifier.weight(1f),
      text =
        buildAnnotatedString {
          if (payee != null) {
            withStyle(payeeStyle) { append(payee) }
            if (category.isNotEmpty()) append(" · ")
          }
          append(category)
        },
      fontSize = dimens.secondLineSize,
      color = colors.pageTextLight,
      overflow = Ellipsis,
      maxLines = 1,
    )

    AmountText(child.amount, dimens, fontSize = dimens.childAmountSize, fontWeight = Normal)
  }

@Composable
private fun SplitChildTableRow(child: Transaction, parent: Transaction, dimens: LedgerDimens) =
  Row(
    modifier =
      Modifier.fillMaxWidth().height(dimens.childRowHeight).padding(horizontal = dimens.rowEnd),
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(dimens.contentGap),
  ) {
    Box(modifier = Modifier.width(DenseColumns.date))

    Text(
      modifier = Modifier.weight(DenseColumns.PAYEE_WEIGHT),
      text = child.payeeUnlike(parent).orEmpty(),
      fontSize = dimens.secondLineSize,
      color = colors.tableText,
      overflow = Ellipsis,
      maxLines = 1,
    )

    Text(
      modifier = Modifier.weight(DenseColumns.CATEGORY_WEIGHT),
      text = categoryText(child, parts = Collapsed, needsCategory = Strings.transactionsNoCategory),
      fontSize = dimens.secondLineSize,
      color = colors.pageTextLight,
      overflow = Ellipsis,
      maxLines = 1,
    )

    AmountText(
      amount = child.amount,
      dimens = dimens,
      modifier = Modifier.width(DenseColumns.amount),
      fontSize = dimens.childAmountSize,
      fontWeight = Normal,
    )
    if (dimens.showBalance) Box(modifier = Modifier.width(DenseColumns.balance))
  }

// A part only names its payee when it differs from the one on the parent's row
private fun Transaction.payeeUnlike(parent: Transaction) = payee.takeIf { it != parent.payee }

@Composable
private fun AmountText(
  amount: Amount,
  dimens: LedgerDimens,
  modifier: Modifier = Modifier,
  fontSize: TextUnit = dimens.amountSize,
  fontWeight: FontWeight = dimens.amountWeight,
) =
  Text(
    modifier = modifier,
    text = amount.formattedString(includeSign = true),
    fontSize = fontSize,
    fontWeight = fontWeight,
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

private val SplitIconSize = 12.dp
private val SplitIconGap = 5.dp
private val ChevronSize = 14.dp
private val SplitInsetStart = 12.dp
private val SplitInsetVertical = 2.dp
private const val HALF_TURN = 180f
private const val DAY_NUMBER_LINE_HEIGHT = 1.1f
private const val LABEL_LENGTH = 3
private const val SHIMMER_BAR_FRACTION = 1.5f

@Preview
@Composable
private fun PreviewLedgerRow(
  @PreviewParameter(LedgerRowProvider::class) params: ColoredParams<LedgerRowParams>,
) =
  PreviewWithColoredParams(params) {
    WithLedgerDimens(density) {
      Column {
        when (density) {
          Comfortable,
          Compact -> LedgerRow(transaction, showDate = showDate, parts = parts)
          Dense -> LedgerTableRow(transaction, parts = parts)
        }
        if (parts != Collapsed) SplitChildren(transaction, density)
      }
    }
  }

@Preview
@Composable
private fun PreviewLedgerShimmerRow(
  @PreviewParameter(DensityProvider::class) params: ColoredParams<TransactionsDensity>,
) = PreviewWithColoredParams(params) { WithLedgerDimens(this) { LedgerShimmerRow() } }

private data class LedgerRowParams(
  val density: TransactionsDensity,
  val transaction: Transaction,
  val showDate: Boolean = true,
  val parts: SplitParts = Collapsed,
)

private class LedgerRowProvider :
  ColoredParameterProvider<LedgerRowParams>(
    LedgerRowParams(Comfortable, TRANSACTION_1),
    LedgerRowParams(Comfortable, TRANSACTION_2, showDate = false),
    LedgerRowParams(Comfortable, TRANSACTION_3),
    LedgerRowParams(Comfortable, TRANSACTION_UNCATEGORISED),
    LedgerRowParams(Comfortable, TRANSACTION_TRANSFER),
    LedgerRowParams(Comfortable, TRANSACTION_OFF_BUDGET),
    LedgerRowParams(Comfortable, TRANSACTION_SPLIT),
    LedgerRowParams(Comfortable, TRANSACTION_SPLIT, parts = Expanded),
    LedgerRowParams(Comfortable, TRANSACTION_SPLIT, parts = Pinned),
    LedgerRowParams(Comfortable, TRANSACTION_SPLIT_CHILD),
    LedgerRowParams(Compact, TRANSACTION_1),
    LedgerRowParams(Compact, TRANSACTION_2, showDate = false),
    LedgerRowParams(Compact, TRANSACTION_3),
    LedgerRowParams(Compact, TRANSACTION_UNCATEGORISED),
    LedgerRowParams(Compact, TRANSACTION_TRANSFER),
    LedgerRowParams(Compact, TRANSACTION_OFF_BUDGET),
    LedgerRowParams(Compact, TRANSACTION_SPLIT),
    LedgerRowParams(Compact, TRANSACTION_SPLIT, parts = Expanded),
    LedgerRowParams(Compact, TRANSACTION_SPLIT, parts = Pinned),
    LedgerRowParams(Compact, TRANSACTION_SPLIT_CHILD),
    LedgerRowParams(Dense, TRANSACTION_1),
    LedgerRowParams(Dense, TRANSACTION_3),
    LedgerRowParams(Dense, TRANSACTION_UNCATEGORISED),
    LedgerRowParams(Dense, TRANSACTION_TRANSFER),
    LedgerRowParams(Dense, TRANSACTION_OFF_BUDGET),
    LedgerRowParams(Dense, TRANSACTION_SPLIT),
    LedgerRowParams(Dense, TRANSACTION_SPLIT, parts = Expanded),
    LedgerRowParams(Dense, TRANSACTION_SPLIT, parts = Pinned),
    LedgerRowParams(Dense, TRANSACTION_SPLIT_CHILD),
  )
