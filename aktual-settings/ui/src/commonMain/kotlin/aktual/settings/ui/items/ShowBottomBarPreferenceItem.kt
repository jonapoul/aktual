package aktual.settings.ui.items

import aktual.core.icons.MaterialIcons
import aktual.core.icons.Visibility
import aktual.core.icons.VisibilityOff
import aktual.core.l10n.Strings
import aktual.core.ui.LocalTheme
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.Theme
import aktual.core.ui.ThemedBooleanParameters
import aktual.core.ui.ThemedParams
import aktual.core.ui.switch
import aktual.settings.ui.BasicPreferenceItem
import aktual.settings.ui.Clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

@Composable
internal fun ShowBottomBarPreferenceItem(
    value: Boolean,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    theme: Theme = LocalTheme.current,
) {
  BasicPreferenceItem(
      modifier = modifier.fillMaxWidth(),
      title = Strings.settingsShowBottomBar,
      subtitle = null,
      icon = icon(value),
      clickability = Clickable { onChange(!value) },
      rightContent = {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        Switch(
            modifier = Modifier.padding(10.dp),
            checked = value,
            onCheckedChange = null,
            colors = theme.switch(isPressed),
        )
      },
  )
}

@Stable
private fun icon(isVisible: Boolean) =
    when (isVisible) {
      true -> MaterialIcons.Visibility
      false -> MaterialIcons.VisibilityOff
    }

@Preview
@Composable
private fun PreviewShowBottomBarPreferenceItem(
    @PreviewParameter(ThemedBooleanParameters::class) params: ThemedParams<Boolean>,
) =
    PreviewWithColorScheme(params.type) {
      ShowBottomBarPreferenceItem(
          value = params.data,
          onChange = {},
      )
    }
