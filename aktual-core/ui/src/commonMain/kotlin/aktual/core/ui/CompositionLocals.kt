package aktual.core.ui

import aktual.budget.model.Amount
import aktual.budget.model.Currency
import aktual.budget.model.CurrencyConfig
import aktual.budget.model.CurrencySymbolPosition
import aktual.budget.model.DateFormat
import aktual.budget.model.NumberFormat
import aktual.budget.model.NumberFormatConfig
import alakazam.compose.VerticalSpacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.DateTimeFormat

val LocalPrivacyEnabled = compositionLocalOf { false }

val LocalBottomStatusBarHeight = compositionLocalOf { 0.dp }

val LocalDateFormatter =
  compositionLocalOf<DateTimeFormat<LocalDate>> { error("No LocalDateFormat value provided") }

val LocalNumberFormatConfig =
  compositionLocalOf<NumberFormatConfig> { error("No NumberFormatConfig value provided") }

val LocalCurrencyConfig =
  compositionLocalOf<CurrencyConfig> { error("No CurrencyConfig value provided") }

val LocalHazeState = compositionLocalOf<HazeState> { error("No HazeState value provided") }

val LocalBlurConfig = compositionLocalOf<BlurConfig> { error("No BlurConfig value provided") }

val LocalDialogBlurState = compositionLocalOf { DialogBlurState() }

/**
 * Tracks how many dialogs are currently showing, so the background blur can animate accordingly.
 */
@Stable
class DialogBlurState {
  var activeDialogCount by mutableIntStateOf(0)
    internal set

  val isActive: Boolean
    get() = activeDialogCount > 0
}

@Composable
fun Amount.formattedString(
  numberFormatConfig: NumberFormatConfig = LocalNumberFormatConfig.current,
  currencyConfig: CurrencyConfig = LocalCurrencyConfig.current,
  includeSign: Boolean = false,
  isPrivacyEnabled: Boolean = LocalPrivacyEnabled.current,
): String = toString(numberFormatConfig, currencyConfig, includeSign, isPrivacyEnabled)

@Composable
fun WithCompositionLocals(
  isPrivacyEnabled: Boolean = false,
  format: NumberFormat = NumberFormat.Default,
  hideFraction: Boolean = false,
  currency: Currency = Currency.None,
  currencyPosition: CurrencySymbolPosition = CurrencySymbolPosition.BeforeAmount,
  addCurrencySpace: Boolean = true,
  dateFormat: DateFormat = DateFormat.Default,
  hazeState: HazeState = rememberHazeState(),
  blurConfig: BlurConfig = remember { BlurConfig() },
  dialogBlurState: DialogBlurState = remember { DialogBlurState() },
  content: @Composable () -> Unit,
) {
  CompositionLocalProvider(
    LocalDateFormatter provides dateFormat.formatter(),
    LocalNumberFormatConfig provides NumberFormatConfig(format, hideFraction),
    LocalCurrencyConfig provides CurrencyConfig(currency, currencyPosition, addCurrencySpace),
    LocalPrivacyEnabled provides isPrivacyEnabled,
    LocalHazeState provides hazeState,
    LocalBlurConfig provides blurConfig,
    LocalDialogBlurState provides dialogBlurState,
    content = content,
  )
}

@Composable
fun BottomStatusBarSpacing(height: Dp = LocalBottomStatusBarHeight.current) = VerticalSpacer(height)
