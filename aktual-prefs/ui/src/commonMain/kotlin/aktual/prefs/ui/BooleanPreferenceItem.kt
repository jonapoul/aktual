package aktual.prefs.ui

import aktual.core.icons.material.Info
import aktual.core.icons.material.MaterialIcons
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.switch
import aktual.prefs.vm.BooleanPreference
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

@Composable
internal fun BooleanPreferenceItem(
  preference: BooleanPreference,
  title: String,
  subtitle: String?,
  icon: ImageVector?,
  modifier: Modifier = Modifier,
  includeBackground: Boolean = true,
  theme: Theme = LocalTheme.current,
  bottomContent: (@Composable ColumnScope.() -> Unit)? = null,
) {
  BooleanPreferenceItem(
    value = preference.value,
    onValueChange = preference.onChange,
    enabled = preference.enabled,
    title = title,
    subtitle = subtitle,
    icon = icon,
    modifier = modifier,
    includeBackground = includeBackground,
    theme = theme,
    bottomContent = bottomContent,
  )
}

@Composable
internal fun BooleanPreferenceItem(
  value: Boolean,
  onValueChange: (Boolean) -> Unit,
  title: String,
  subtitle: String?,
  icon: ImageVector?,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  includeBackground: Boolean = true,
  theme: Theme = LocalTheme.current,
  bottomContent: (@Composable ColumnScope.() -> Unit)? = null,
) {
  BasicPreferenceItem(
    modifier = modifier,
    title = title,
    subtitle = subtitle,
    icon = icon,
    enabled = enabled,
    includeBackground = includeBackground,
    onClick = { onValueChange(!value) },
    bottomContent = bottomContent,
    rightContent = {
      Switch(
        modifier = Modifier.padding(10.dp),
        checked = value,
        onCheckedChange = null,
        enabled = enabled,
        colors = theme.switch(),
      )
    },
  )
}

@Preview
@Composable
private fun PreviewBooleanPreferenceItem(
  @PreviewParameter(BooleanPreferenceItemProvider::class)
  params: ThemedParams<BooleanPreferenceItemParams>
) =
  PreviewWithColorScheme(params.theme) {
    BooleanPreferenceItem(
      value = params.data.value,
      onValueChange = {},
      title = params.data.title,
      subtitle = params.data.subtitle,
      icon = params.data.icon,
      enabled = params.data.enabled,
    )
  }

private data class BooleanPreferenceItemParams(
  val value: Boolean,
  val title: String,
  val subtitle: String?,
  val icon: ImageVector?,
  val enabled: Boolean = true,
)

private class BooleanPreferenceItemProvider :
  ThemedParameterProvider<BooleanPreferenceItemParams>(
    BooleanPreferenceItemParams(
      value = true,
      title = "Change the doodad",
      subtitle =
        "When you change this setting, the doodad will update. This might also affect the thingybob.",
      icon = MaterialIcons.Info,
    ),
    BooleanPreferenceItemParams(
      value = false,
      title = "This one has no subtitle and no icon",
      subtitle = null,
      icon = null,
    ),
    BooleanPreferenceItemParams(
      value = true,
      title = "Disabled preference",
      subtitle = "This item is disabled and should appear subdued",
      icon = MaterialIcons.Info,
      enabled = false,
    ),
  )
