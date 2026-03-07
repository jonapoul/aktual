package aktual.settings.ui.root

import aktual.core.icons.MaterialIcons
import aktual.core.icons.Visibility
import aktual.core.icons.VisibilityOff
import aktual.core.l10n.Strings
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.ThemedBooleanParameters
import aktual.core.ui.ThemedParams
import aktual.settings.vm.BooleanPreference
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

@Composable
internal fun ShowBottomBarPreferenceItem(
  preference: BooleanPreference,
  modifier: Modifier = Modifier,
) {
  _root_ide_package_.aktual.settings.ui.BooleanPreferenceItem(
    modifier = modifier,
    value = preference.value,
    onValueChange = preference.onValueChange,
    title = Strings.settingsShowBottomBar,
    subtitle = null,
    icon = remember(preference.value) { icon(preference.value) },
    bottomContent = null,
  )
}

@Stable
private fun icon(isVisible: Boolean) =
  if (isVisible) MaterialIcons.Visibility else MaterialIcons.VisibilityOff

@Preview
@Composable
private fun PreviewShowBottomBarPreferenceItem(
  @PreviewParameter(ThemedBooleanParameters::class) params: ThemedParams<Boolean>
) =
  PreviewWithColorScheme(params.theme) {
    ShowBottomBarPreferenceItem(
      preference = BooleanPreference(value = params.data, onValueChange = {})
    )
  }
