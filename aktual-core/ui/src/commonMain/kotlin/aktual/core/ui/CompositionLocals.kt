package aktual.core.ui

import aktual.budget.model.Amount
import aktual.budget.model.NumberFormat
import aktual.budget.model.NumberFormatConfig
import alakazam.compose.VerticalSpacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.hazeEffect

val LocalPrivacyEnabled = compositionLocalOf { false }

val LocalBottomStatusBarHeight = compositionLocalOf { 0.dp }

val LocalNumberFormatConfig =
  compositionLocalOf<NumberFormatConfig> { error("No NumberFormatConfig value provided") }

val LocalHazeState = compositionLocalOf<HazeState> { error("No HazeState supplied") }

@Composable
fun WithHazeState(state: HazeState, content: @Composable () -> Unit) =
  CompositionLocalProvider(LocalHazeState provides state, content)

@Composable
fun Modifier.aktualHaze(
  state: HazeState = LocalHazeState.current,
  style: HazeStyle = LocalHazeStyle.current,
): Modifier = hazeEffect(state, style)

@Composable
fun Amount.formattedString(
  config: NumberFormatConfig = LocalNumberFormatConfig.current,
  includeSign: Boolean = false,
  isPrivacyEnabled: Boolean = LocalPrivacyEnabled.current,
): String = toString(config, includeSign, isPrivacyEnabled)

@Composable
fun WithCompositionLocals(
  isPrivacyEnabled: Boolean = false,
  format: NumberFormat = NumberFormat.Default,
  hideFraction: Boolean = false,
  content: @Composable () -> Unit,
) {
  CompositionLocalProvider(
    LocalNumberFormatConfig provides NumberFormatConfig(format, hideFraction),
    LocalPrivacyEnabled provides isPrivacyEnabled,
  ) {
    content()
  }
}

@Composable
fun BottomStatusBarSpacing(height: Dp = LocalBottomStatusBarHeight.current) = VerticalSpacer(height)
