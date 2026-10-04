package aktual.prefs.ui.root

import aktual.core.icons.AktualIcons
import aktual.core.icons.Calendar3
import aktual.core.icons.material.ArrowRight
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.ThemeRoutine
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.NormalIconButton
import aktual.core.ui.PreviewWithColoredParams
import aktual.prefs.ui.core.BasicPreferenceItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

/** Opens a separate settings screen. */
@Composable
internal fun SubSettingsItem(
  title: String,
  icon: ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  BasicPreferenceItem(
    modifier = modifier,
    title = title,
    subtitle = null,
    icon = icon,
    onClick = onClick,
    rightContent = {
      NormalIconButton(
        contentDescription = title,
        imageVector = MaterialIcons.ArrowRight,
        onClick = onClick,
      )
    },
  )
}

@Preview
@Composable
private fun PreviewSubSettingsItem(
  @PreviewParameter(SubSettingsItemProvider::class) params: ColoredParams<SubSettingsItemParams>
) = PreviewWithColoredParams(params) { SubSettingsItem(title = title, icon = icon, onClick = {}) }

private data class SubSettingsItemParams(val title: String, val icon: ImageVector)

private class SubSettingsItemProvider :
  ColoredParameterProvider<SubSettingsItemParams>(
    SubSettingsItemParams(title = "App theme", icon = MaterialIcons.ThemeRoutine),
    SubSettingsItemParams(title = "Schedules", icon = AktualIcons.Calendar3),
  )
