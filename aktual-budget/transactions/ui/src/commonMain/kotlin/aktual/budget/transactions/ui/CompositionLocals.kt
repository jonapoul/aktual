package aktual.budget.transactions.ui

import aktual.budget.model.TransactionsDensity
import aktual.core.ui.AktualTheme.textSizes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

internal val LocalLedgerDimens = compositionLocalOf { LedgerDimens(Default) }

@Composable
internal fun WithLedgerDimens(
  density: TransactionsDensity,
  showBalance: Boolean = true,
  showAccount: Boolean = true,
  content: @Composable () -> Unit,
) =
  CompositionLocalProvider(
    LocalLedgerDimens provides
      remember(density, showBalance, showAccount) {
        val dimens = LedgerDimens(density)
        dimens.copy(showBalance = showBalance, showAccount = dimens.showAccount && showAccount)
      },
    content = content,
  )

@Immutable
internal data class LedgerDimens(
  val rowHeight: Dp,
  val rowVertical: Dp,
  val railWidth: Dp,
  val railTop: Dp,
  val contentGap: Dp,
  val rowEnd: Dp,
  val dayNumberSize: TextUnit,
  val weekdaySize: TextUnit,
  val payeeSize: TextUnit,
  val payeeWeight: FontWeight,
  val secondLineSize: TextUnit,
  val amountSize: TextUnit,
  val amountWeight: FontWeight,
  val childRowHeight: Dp,
  val childAmountSize: TextUnit,
  val balanceSize: TextUnit = textSizes.small,
  val showAccount: Boolean = false,
  val showNotes: Boolean = false,
  val showBalance: Boolean = true,
)

internal fun LedgerDimens(density: TransactionsDensity): LedgerDimens =
  when (density) {
    Comfortable ->
      LedgerDimens(
        rowHeight = 60.dp,
        rowVertical = 8.dp,
        railWidth = 60.dp,
        railTop = 12.dp,
        contentGap = 12.dp,
        rowEnd = 16.dp,
        dayNumberSize = textSizes.extraLarge,
        weekdaySize = textSizes.extraSmall,
        payeeSize = textSizes.mediumPlus,
        payeeWeight = SemiBold,
        secondLineSize = textSizes.smallPlus,
        amountSize = textSizes.mediumPlus,
        amountWeight = SemiBold,
        childRowHeight = 36.dp,
        childAmountSize = textSizes.medium,
        showAccount = true,
        showNotes = true,
      )

    Compact ->
      LedgerDimens(
        rowHeight = 44.dp,
        rowVertical = 4.dp,
        railWidth = 48.dp,
        railTop = 8.dp,
        contentGap = 10.dp,
        rowEnd = 12.dp,
        dayNumberSize = textSizes.mediumPlus,
        weekdaySize = textSizes.tiny,
        payeeSize = textSizes.medium,
        payeeWeight = Medium,
        secondLineSize = textSizes.small,
        amountSize = textSizes.medium,
        amountWeight = SemiBold,
        childRowHeight = 32.dp,
        childAmountSize = textSizes.smallPlus,
      )

    Dense ->
      LedgerDimens(
        rowHeight = 32.dp,
        rowVertical = 0.dp,
        railWidth = 40.dp,
        railTop = 0.dp,
        contentGap = 8.dp,
        rowEnd = 10.dp,
        dayNumberSize = textSizes.small,
        weekdaySize = textSizes.small,
        payeeSize = textSizes.smallPlus,
        payeeWeight = Medium,
        secondLineSize = textSizes.small,
        amountSize = textSizes.smallPlus,
        amountWeight = Medium,
        childRowHeight = 28.dp,
        childAmountSize = textSizes.small,
      )
  }

// How a split parent's row stands with its parts: closed, open, or always open with no toggle
internal enum class SplitParts {
  Collapsed,
  Expanded,
  Pinned,
}

// Column layout shared by the Dense header and rows
internal object DenseColumns {
  val date = 40.dp
  val amount = 74.dp
  val balance = 68.dp
  const val PAYEE_WEIGHT = 1.2f
  const val CATEGORY_WEIGHT = 1f
}
