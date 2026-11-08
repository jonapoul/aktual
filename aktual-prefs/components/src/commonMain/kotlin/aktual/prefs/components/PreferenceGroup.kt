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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

// Title of the group a child item is in. Child items drop their own card background so they share
// the group's
internal val LocalPreferenceGroupTitle = compositionLocalOf<String?> { null }

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
      CompositionLocalProvider(LocalPreferenceGroupTitle provides title) { content() }
    },
  )
}

@Preview
@Composable
private fun PreviewPreferenceGroup(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    PreferenceGroup(title = DOODAD_GROUP) {
      BooleanPreferenceItem(
        value = true,
        onValueChange = {},
        title = DOODAD_TOGGLE,
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

// Loops the highlight on and off. Run it in interactive mode to see the fade
@Preview
@Composable
private fun PreviewHighlightAnimation(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    var isHighlighted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
      while (true) {
        delay(1.seconds)
        isHighlighted = !isHighlighted
      }
    }

    val highlighter = remember {
      PreferenceHighlighter(group = DOODAD_GROUP, title = DOODAD_TOGGLE, onFinish = {})
    }
    CompositionLocalProvider(
      LocalPreferenceHighlighter provides highlighter.takeIf { isHighlighted },
    ) {
      DoodadGroup()
    }
  }

@Composable
private fun DoodadGroup() =
  PreferenceGroup(title = DOODAD_GROUP) {
    BooleanPreferenceItem(
      value = true,
      onValueChange = {},
      title = DOODAD_TOGGLE,
      subtitle = "Turns the doodad on and off",
      icon = AktualIcons.InformationOutline,
    )
    BooleanPreferenceItem(
      value = false,
      onValueChange = {},
      title = "Another doodad",
      subtitle = null,
      icon = null,
    )
  }

private const val DOODAD_GROUP = "Doodad settings"
private const val DOODAD_TOGGLE = "Enable the doodad"
