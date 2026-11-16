package aktual.prefs.ui.bars

import aktual.core.icons.material.BlurCircular
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Opacity
import aktual.core.icons.material.RoundedCorner
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.SettingsHighlight
import aktual.core.theme.Colors
import aktual.core.ui.ColorParameters
import aktual.core.ui.LocalHazeConfig
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColors
import aktual.prefs.ui.PreviewOptionGroup
import aktual.prefs.ui.PreviewSettingsScaffold
import aktual.prefs.ui.SliderPreferenceItem
import aktual.prefs.vm.ListPreference
import aktual.prefs.vm.SliderPreference
import aktual.prefs.vm.bars.BarSettingsState
import aktual.prefs.vm.bars.BarSettingsViewModel
import aktual.prefs.vm.bars.GlassCornerRadiusPreference
import aktual.prefs.vm.systemui.BlurAlphaPreference
import aktual.prefs.vm.systemui.BlurRadiusPreference
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun BarSettingsScreen(
  back: BackNavigator,
  highlight: SettingsHighlight?,
  viewModel: BarSettingsViewModel = metroViewModel<BarSettingsViewModel>(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  BarSettingsScaffold(state = state, highlight = highlight, onBack = back::invoke)
}

@Composable
private fun BarSettingsScaffold(
  state: BarSettingsState,
  onBack: () -> Unit,
  highlight: SettingsHighlight? = null,
) {
  // Sliders only save when released, so the preview follows these while one is being dragged
  val blurRadius = rememberDraft(state.blurRadius)
  val blurAlpha = rememberDraft(state.blurAlpha)
  val cornerRadius = rememberDraft(state.glassCornerRadiusDp)
  val config =
    LocalHazeConfig.current.copy(
      appBarEffect = state.barEffect.value,
      barBlurRadius = blurRadius.floatValue.dp,
      barBlurAlpha = blurAlpha.floatValue,
      glassStyle = state.glassStyle.value,
      glassCornerRadius = cornerRadius.floatValue.dp,
    )

  PreviewSettingsScaffold(
    title = Strings.settingsUiBarsTitle,
    description = Strings.settingsBarsDesc,
    onBack = onBack,
    preview = { BarsPreview(config) },
    highlight = highlight,
  ) {
    item {
      PreviewOptionGroup(
        title = Strings.settingsUiBarEffect,
        preference = state.barEffect,
        label = { it.label() },
        preview = { OptionIcon(it.icon) },
      )
    }
    item {
      DraftSlider(
        preference = state.blurRadius,
        onValueChanging = { blurRadius.floatValue = it },
        title = Strings.settingsUiBlurRadius,
        icon = MaterialIcons.BlurCircular,
      )
    }
    item {
      DraftSlider(
        preference = state.blurAlpha,
        onValueChanging = { blurAlpha.floatValue = it },
        title = Strings.settingsUiBlurAlpha,
        icon = MaterialIcons.Opacity,
      )
    }
    item {
      PreviewOptionGroup(
        title = Strings.settingsUiGlassStyle,
        preference = state.glassStyle,
        label = { it.label() },
        preview = { OptionIcon(it.icon) },
      )
    }
    item {
      DraftSlider(
        preference = state.glassCornerRadiusDp,
        onValueChanging = { cornerRadius.floatValue = it },
        title = Strings.settingsUiGlassCornerRadius,
        icon = MaterialIcons.RoundedCorner,
      )
    }
  }
}

@Composable
private fun rememberDraft(preference: SliderPreference): MutableFloatState =
  remember(preference.value) { mutableFloatStateOf(preference.value) }

@Composable
@NonRestartableComposable
private fun DraftSlider(
  preference: SliderPreference,
  title: String,
  icon: ImageVector,
  onValueChanging: (Float) -> Unit,
) =
  SliderPreferenceItem(
    preference = preference,
    title = title,
    subtitle = null,
    icon = icon,
    onValueChanging = onValueChanging,
  )

@PortraitPreview
@Composable
private fun PreviewBarSettingsScaffold(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    BarSettingsScaffold(
      onBack = {},
      state =
        BarSettingsState(
          barEffect = ListPreference(Blur),
          blurRadius = BlurRadiusPreference(5f),
          blurAlpha = BlurAlphaPreference(0.5f),
          glassStyle = ListPreference(Regular, enabled = false),
          glassCornerRadiusDp = GlassCornerRadiusPreference(24f, enabled = false),
        ),
    )
  }
