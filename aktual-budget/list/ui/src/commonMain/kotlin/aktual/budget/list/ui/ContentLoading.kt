package aktual.budget.list.ui

import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ColoredParameters
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColors
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

@Composable
internal fun ContentLoading(modifier: Modifier = Modifier) {
  Box(modifier = modifier, contentAlignment = Alignment.Center) {
    CircularProgressIndicator(
      modifier = Modifier.size(50.dp),
      color = colors.buttonPrimaryBackground,
    )
  }
}

@PortraitPreview
@Composable
private fun PreviewContentLoading(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) { ContentLoading() }
