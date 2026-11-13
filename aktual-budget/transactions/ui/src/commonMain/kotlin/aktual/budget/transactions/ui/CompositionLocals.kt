package aktual.budget.transactions.ui

import aktual.budget.model.TransactionsDensity
import aktual.budget.tags.domain.TagLabel
import aktual.core.theme.hasAlternateRowColour
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.textSizes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal val LocalLedgerDimens = compositionLocalOf { LedgerDimens(Default) }

internal val LocalTagLabels =
  staticCompositionLocalOf<ImmutableList<TagLabel>> { persistentListOf() }

@Composable
internal fun WithLedgerDimens(
  density: TransactionsDensity,
  showBalance: Boolean = true,
  showAccount: Boolean = true,
  isReconciling: Boolean = false,
  alternateRowColours: Boolean = false,
  content: @Composable () -> Unit,
) {
  // Only themes with their own alternate row colour shade rows, see hasAlternateRowColour
  val shadeRows = alternateRowColours && colors.hasAlternateRowColour
  CompositionLocalProvider(
    LocalLedgerDimens provides
      remember(density, showBalance, showAccount, isReconciling, shadeRows) {
        val dimens = LedgerDimens(density)
        dimens.copy(
          showBalance = showBalance,
          showAccount = dimens.showAccount && showAccount,
          isReconciling = isReconciling,
          shadeRows = shadeRows,
        )
      },
    content = content,
  )
}

@Immutable
internal data class LedgerDimens(
  val density: TransactionsDensity,
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
  // Rows end in a column to clear them, as upstream's do while reconciling
  val isReconciling: Boolean = false,
  val shadeRows: Boolean = false,
)

internal fun LedgerDimens(density: TransactionsDensity): LedgerDimens =
  when (density) {
    Comfortable ->
      LedgerDimens(
        density = density,
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
        density = density,
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
        density = density,
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
  val cleared = 32.dp
  const val PAYEE_WEIGHT = 1.2f
  const val CATEGORY_WEIGHT = 1f
}
