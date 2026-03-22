package aktual.core.ui

import aktual.budget.model.Amount
import aktual.budget.model.Currency
import aktual.budget.model.CurrencyConfig
import aktual.budget.model.CurrencySymbolPosition
import aktual.budget.model.NumberFormat
import aktual.budget.model.NumberFormatConfig
import alakazam.compose.VerticalSpacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState

val LocalPrivacyEnabled = compositionLocalOf { false }

val LocalBottomStatusBarHeight = compositionLocalOf { 0.dp }

val LocalNumberFormatConfig =
  compositionLocalOf<NumberFormatConfig> { error("No NumberFormatConfig value provided") }

val LocalCurrencyConfig =
  compositionLocalOf<CurrencyConfig> { error("No CurrencyConfig value provided") }

val LocalHazeState = compositionLocalOf<HazeState> { error("No HazeState value provided") }

val LocalBlurConfig = compositionLocalOf<BlurConfig> { error("No BlurConfig value provided") }

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
  hazeState: HazeState = rememberHazeState(),
  blurConfig: BlurConfig = BlurConfig(),
  content: @Composable () -> Unit,
) {
  CompositionLocalProvider(
    LocalNumberFormatConfig provides NumberFormatConfig(format, hideFraction),
    LocalCurrencyConfig provides CurrencyConfig(currency, currencyPosition, addCurrencySpace),
    LocalPrivacyEnabled provides isPrivacyEnabled,
    LocalHazeState provides hazeState,
    LocalBlurConfig provides blurConfig,
    content = content,
  )
}

@Composable
fun BottomStatusBarSpacing(height: Dp = LocalBottomStatusBarHeight.current) = VerticalSpacer(height)
