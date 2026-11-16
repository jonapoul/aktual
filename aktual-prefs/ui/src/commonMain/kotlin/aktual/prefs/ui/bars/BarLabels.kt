package aktual.prefs.ui.bars

import aktual.budget.model.BarEffect
import aktual.budget.model.BarGlassStyle
import aktual.core.l10n.Strings
import androidx.compose.runtime.Composable

@Composable
internal fun BarEffect.label(): String =
  when (this) {
    None -> Strings.settingsUiBarEffectNone
    Blur -> Strings.settingsUiBarEffectBlur
    Glass -> Strings.settingsUiBarEffectGlass
  }

@Composable
internal fun BarGlassStyle.label(): String =
  when (this) {
    Regular -> Strings.settingsUiGlassStyleRegular
    Clear -> Strings.settingsUiGlassStyleClear
  }
