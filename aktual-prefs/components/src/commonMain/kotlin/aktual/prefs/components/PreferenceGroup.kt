package aktual.prefs.components

import aktual.core.icons.AktualIcons
import aktual.core.icons.InformationOutline
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.ColorParameters
import aktual.core.ui.PreviewWithColors
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

// Child items drop their own card background so they share the group's
internal val LocalInPreferenceGroup = compositionLocalOf { false }

@Composable
fun PreferenceGroup(
  title: String,
  modifier: Modifier = Modifier,
  subtitle: String? = null,
  enabled: Boolean = true,
  content: @Composable ColumnScope.() -> Unit,
) {
  BasicPreferenceItem(
    title = title,
    subtitle = subtitle,
    icon = null,
    onClick = null,
    modifier = modifier,
    enabled = enabled,
    includeBackground = true,
    rightContent = null,
    headerStyle = typography.titleMedium,
    bottomContent = {
      CompositionLocalProvider(LocalInPreferenceGroup provides true) { content() }
    },
  )
}

@Preview
@Composable
private fun PreviewPreferenceGroup(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    PreferenceGroup(title = "Doodad settings") {
      BooleanPreferenceItem(
        value = true,
        onValueChange = {},
        title = "Enable the doodad",
        subtitle = "Turns the doodad on and off",
        icon = AktualIcons.InformationOutline,
      )
      BooleanPreferenceItem(
        value = false,
        onValueChange = {},
        title = "Disabled child",
        subtitle = null,
        icon = null,
        enabled = false,
      )
    }
  }
