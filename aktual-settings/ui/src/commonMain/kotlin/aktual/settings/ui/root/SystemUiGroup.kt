package aktual.settings.ui.root

import aktual.core.icons.material.BlurOn
import aktual.core.icons.material.LinearScale
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.TransitionDissolve
import aktual.core.icons.material.Visibility
import aktual.core.icons.material.VisibilityOff
import aktual.core.l10n.Strings
import aktual.settings.ui.BooleanPreferenceItem
import aktual.settings.ui.PreferenceGroup
import aktual.settings.ui.SliderPreferenceItem
import aktual.settings.vm.root.SystemUiConfigState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun SystemUiGroup(state: SystemUiConfigState, modifier: Modifier = Modifier) {
  PreferenceGroup(
    title = Strings.settingsUiGroup,
    subtitle = Strings.settingsUiDesc,
    modifier = modifier,
  ) {
    BooleanPreferenceItem(
      preference = state.showStatusBar,
      title = Strings.settingsUiShowStatus,
      subtitle = null,
      includeBackground = false,
      icon =
        if (state.showStatusBar.value) MaterialIcons.Visibility else MaterialIcons.VisibilityOff,
    )

    BooleanPreferenceItem(
      preference = state.blurStatusBar,
      title = Strings.settingsUiBlurStatus,
      subtitle = Strings.settingsUiEnableBlurSubtitle,
      icon = MaterialIcons.BlurOn,
      includeBackground = false,
    )

    BooleanPreferenceItem(
      preference = state.blurTopBar,
      title = Strings.settingsUiBlurTop,
      subtitle = Strings.settingsUiEnableBlurSubtitle,
      icon = MaterialIcons.BlurOn,
      includeBackground = false,
    )

    SliderPreferenceItem(
      preference = state.blurRadiusDp,
      title = Strings.settingsUiBlurRadius,
      subtitle = null,
      icon = MaterialIcons.LinearScale,
      includeBackground = false,
    )

    SliderPreferenceItem(
      preference = state.blurAlpha,
      title = Strings.settingsUiBlurAlpha,
      subtitle = null,
      icon = MaterialIcons.TransitionDissolve,
      includeBackground = false,
    )
  }
}
