package aktual.prefs.ui.root

import aktual.core.icons.material.BlurOn
import aktual.core.icons.material.Dialogs
import aktual.core.icons.material.LinearScale
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.TransitionDissolve
import aktual.core.icons.material.Visibility
import aktual.core.icons.material.VisibilityOff
import aktual.core.l10n.Strings
import aktual.prefs.ui.BooleanPreferenceItem
import aktual.prefs.ui.PreferenceGroup
import aktual.prefs.ui.SliderPreferenceItem
import aktual.prefs.vm.root.SystemUiConfigState
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
      preference = state.blurAppBars,
      title = Strings.settingsUiBlurBars,
      subtitle = null,
      icon = MaterialIcons.BlurOn,
      includeBackground = false,
    )

    BooleanPreferenceItem(
      preference = state.blurDialogs,
      title = Strings.settingsUiBlurDialogs,
      subtitle = null,
      icon = MaterialIcons.Dialogs,
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
