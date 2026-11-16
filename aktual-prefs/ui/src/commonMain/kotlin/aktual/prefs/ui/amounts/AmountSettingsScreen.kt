package aktual.prefs.ui.amounts

import aktual.budget.model.Amount
import aktual.budget.model.NumberFormatConfig
import aktual.core.icons.material.DecimalDecrease
import aktual.core.icons.material.DecimalIncrease
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.SettingsHighlight
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.ColorParameters
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColors
import aktual.prefs.ui.BooleanPreferenceItem
import aktual.prefs.ui.PreviewOptionGroup
import aktual.prefs.ui.PreviewSettingsScaffold
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.ListPreference
import aktual.prefs.vm.amounts.AmountSettingsState
import aktual.prefs.vm.amounts.AmountSettingsViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun AmountSettingsScreen(
  back: BackNavigator,
  highlight: SettingsHighlight?,
  viewModel: AmountSettingsViewModel = metroViewModel<AmountSettingsViewModel>(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  AmountSettingsScaffold(state = state, highlight = highlight, onBack = back::invoke)
}

@Composable
private fun AmountSettingsScaffold(
  state: AmountSettingsState,
  onBack: () -> Unit,
  highlight: SettingsHighlight? = null,
) {
  PreviewSettingsScaffold(
    title = Strings.settingsAmounts,
    description = Strings.settingsAmountsDesc,
    onBack = onBack,
    preview = { AmountsPreview(state) },
    highlight = highlight,
  ) {
    item {
      PreviewOptionGroup(
        title = Strings.settingsFormatNumbers,
        preference = state.numberFormat,
        label = { format ->
          LargeAmount.format(state, NumberFormatConfig(format, state.hideFraction.value))
        },
      )
    }
    item {
      BooleanPreferenceItem(
        preference = state.hideFraction,
        title = Strings.settingsFormatDecimal,
        subtitle = null,
        icon =
          if (state.hideFraction.value) {
            MaterialIcons.DecimalDecrease
          } else {
            MaterialIcons.DecimalIncrease
          },
      )
    }
    item { CurrencyGroup(state) }
  }
}

@Composable
private fun AmountsPreview(state: AmountSettingsState) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Text(
      text = LargeAmount.format(state),
      style = typography.headlineMedium,
      color = colors.pageText,
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
      SmallAmounts.fastForEach { amount ->
        Text(
          text = amount.format(state),
          style = typography.bodyLarge,
          color = amount.color(),
        )
      }
    }
  }
}

// Privacy mode is ignored so the preview always shows the digits being formatted
private fun Amount.format(
  state: AmountSettingsState,
  numberFormatConfig: NumberFormatConfig = state.numberFormatConfig,
): String =
  toString(
    numberFormatConfig = numberFormatConfig,
    currencyConfig = state.currencyConfig,
    includeSign = false,
    isPrivacyEnabled = false,
  )

@Composable
@ReadOnlyComposable
private fun Amount.color(): Color =
  when {
    this == Zero -> colors.numberNeutral
    isPositive() -> colors.numberPositive
    else -> colors.numberNegative
  }

private val LargeAmount = Amount(1_234_567.89)
private val SmallAmounts = listOf(Amount(42.5), Amount(-1_250.0), Amount.Zero)

@PortraitPreview
@Composable
private fun PreviewAmountSettingsScaffold(
  @PreviewParameter(ColorParameters::class) colors: Colors,
) =
  PreviewWithColors(colors) {
    AmountSettingsScaffold(
      onBack = {},
      state =
        AmountSettingsState(
          numberFormat = ListPreference(DotComma),
          hideFraction = BooleanPreference(false),
          currency = ListPreference(Euro),
          symbolPosition = ListPreference(AfterAmount),
          spaceBetweenAmountAndSymbol = BooleanPreference(true),
        ),
    )
  }
