@file:Suppress("ComposeCompositionLocalUsage")

package aktual.budget.transactions.ui

import aktual.budget.model.TransactionsDensity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal val LocalLedgerDimens = compositionLocalOf { LedgerDimens(Default) }

@Composable
internal fun WithLedgerDimens(
  density: TransactionsDensity,
  showBalance: Boolean = true,
  content: @Composable () -> Unit,
) =
  CompositionLocalProvider(
    LocalLedgerDimens provides
      remember(density, showBalance) { LedgerDimens(density).copy(showBalance = showBalance) },
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
  val balanceSize: TextUnit = 12.sp,
  val showAccount: Boolean = false,
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
        dayNumberSize = 20.sp,
        weekdaySize = 11.sp,
        payeeSize = 15.sp,
        payeeWeight = SemiBold,
        secondLineSize = 13.sp,
        amountSize = 15.sp,
        amountWeight = SemiBold,
        showAccount = true,
      )

    Compact ->
      LedgerDimens(
        rowHeight = 44.dp,
        rowVertical = 4.dp,
        railWidth = 48.dp,
        railTop = 8.dp,
        contentGap = 10.dp,
        rowEnd = 12.dp,
        dayNumberSize = 15.sp,
        weekdaySize = 10.sp,
        payeeSize = 14.sp,
        payeeWeight = Medium,
        secondLineSize = 12.sp,
        amountSize = 14.sp,
        amountWeight = SemiBold,
      )

    Dense ->
      LedgerDimens(
        rowHeight = 32.dp,
        rowVertical = 0.dp,
        railWidth = 40.dp,
        railTop = 0.dp,
        contentGap = 8.dp,
        rowEnd = 10.dp,
        dayNumberSize = 12.sp,
        weekdaySize = 12.sp,
        payeeSize = 13.sp,
        payeeWeight = Medium,
        secondLineSize = 12.sp,
        amountSize = 13.sp,
        amountWeight = Medium,
      )
  }

// Column layout shared by the Dense header and rows
internal object DenseColumns {
  val date = 40.dp
  val amount = 74.dp
  val balance = 68.dp
  const val PAYEE_WEIGHT = 1.2f
  const val CATEGORY_WEIGHT = 1f
}
