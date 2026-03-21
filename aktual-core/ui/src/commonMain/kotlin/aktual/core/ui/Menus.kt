package aktual.core.ui

import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties

@Composable
fun ThemedDropdownMenu(
  expanded: Boolean,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  offset: DpOffset = DpOffset(0.dp, 0.dp),
  scrollState: ScrollState = rememberScrollState(),
  properties: PopupProperties = PopupProperties(focusable = true),
  theme: Theme = LocalTheme.current,
  content: @Composable ColumnScope.() -> Unit,
) {
  DropdownMenu(
    expanded = expanded,
    onDismissRequest = onDismissRequest,
    modifier = modifier,
    offset = offset,
    scrollState = scrollState,
    properties = properties,
    containerColor = theme.menuBackground,
    content = content,
  )
}

@Composable
fun ThemedDropdownMenuItem(
  text: @Composable () -> Unit,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  leadingIcon: @Composable (() -> Unit)? = null,
  trailingIcon: @Composable (() -> Unit)? = null,
  enabled: Boolean = true,
  contentPadding: PaddingValues = MenuDefaults.DropdownMenuItemContentPadding,
  interactionSource: MutableInteractionSource? = remember { MutableInteractionSource() },
  theme: Theme = LocalTheme.current,
) {
  val colors = theme.dropDownMenuItem()
  DropdownMenuItem(
    text = {
      // DropdownMenu sets LocalTextStyle to labelLarge, which has an explicit color
      // (pageTextSubdued). Override it so MenuItemColors.textColor takes effect.
      ProvideTextStyle(LocalTextStyle.current.copy(color = colors.textColor)) { text() }
    },
    onClick = onClick,
    modifier = modifier,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon,
    enabled = enabled,
    colors = colors,
    contentPadding = contentPadding,
    interactionSource = interactionSource,
  )
}
