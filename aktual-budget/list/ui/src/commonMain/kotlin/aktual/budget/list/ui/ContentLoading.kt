package aktual.budget.list.ui

import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithTheme
import aktual.core.ui.ThemeParameters
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

@Composable
internal fun ContentLoading(modifier: Modifier = Modifier, theme: Theme = LocalTheme.current) {
  Box(modifier = modifier, contentAlignment = Alignment.Center) {
    CircularProgressIndicator(
      modifier = Modifier.size(50.dp),
      color = theme.buttonPrimaryBackground,
    )
  }
}

@PortraitPreview
@Composable
private fun PreviewContentLoading(@PreviewParameter(ThemeParameters::class) theme: Theme) =
  PreviewWithTheme(theme) { ContentLoading() }
