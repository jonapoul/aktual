package aktual.budget.list.ui

import aktual.core.ui.AktualProgressIndicator
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ColorParameters
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColors
import aktual.theme.Colors
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

@Composable
internal fun ContentLoading(modifier: Modifier = Modifier) {
  Box(modifier = modifier, contentAlignment = Center) {
    AktualProgressIndicator(size = 50.dp, color = colors.accent)
  }
}

@PortraitPreview
@Composable
private fun PreviewContentLoading(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) { ContentLoading() }
