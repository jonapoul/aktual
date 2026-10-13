package aktual.prefs.ui.root

import aktual.core.icons.AktualIcons
import aktual.core.icons.Calendar3
import aktual.core.icons.material.ArrowRight
import aktual.core.icons.material.MaterialIcons
import aktual.core.theme.Colors
import aktual.core.ui.ColoredParameters
import aktual.core.ui.IconButtonColorProvider
import aktual.core.ui.NormalIconButton
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.disabled
import aktual.core.ui.normalIconButton
import aktual.prefs.ui.core.BasicPreferenceItem
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

@Composable
internal fun SubSettingsItem(
  title: String,
  icon: ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isEnabled: Boolean = true,
) {
  BasicPreferenceItem(
    modifier = modifier,
    title = title,
    subtitle = null,
    icon = icon,
    onClick = onClick,
    enabled = isEnabled,
    rightContent = {
      NormalIconButton(
        contentDescription = title,
        imageVector = MaterialIcons.ArrowRight,
        colors = NavItemButtonColors,
        isEnabled = isEnabled,
        onClick = onClick,
      )
    },
  )
}

private val NavItemButtonColors = IconButtonColorProvider { colors, isPressed ->
  colors
    .normalIconButton(isPressed)
    .copy(
      containerColor = colors.settingsNavItemBackgroundSelected,
      disabledContainerColor = colors.settingsCardBorder.disabled,
      contentColor = colors.settingsNavItemTextSelected,
      disabledContentColor = colors.settingsCardTextSubdued.disabled,
    )
}

@Preview
@Composable
private fun PreviewSubSettingsItem(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
      listOf(true, false).forEach { isEnabled ->
        SubSettingsItem(
          title = "Schedules",
          icon = AktualIcons.Calendar3,
          isEnabled = isEnabled,
          onClick = {},
        )
      }
    }
  }
