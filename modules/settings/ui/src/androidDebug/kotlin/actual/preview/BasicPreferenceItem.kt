package actual.preview

import actual.core.ui.PreviewThemedColumn
import actual.settings.ui.BasicPreferenceItem
import actual.settings.ui.Clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun WithIcon() = PreviewThemedColumn {
  BasicPreferenceItem(
    title = "Change the doodad",
    subtitle = "When you change this setting, the doodad will update. This might also affect the thingybob.",
    icon = Icons.Filled.Info,
    clickability = Clickable { },
  )
}

@Preview
@Composable
private fun WithoutIcon() = PreviewThemedColumn {
  BasicPreferenceItem(
    title = "Change the doodad",
    subtitle = "When you change this setting, the doodad will update. This might also affect the thingybob.",
    icon = null,
    clickability = Clickable { },
  )
}
