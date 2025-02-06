package actual.budget.list.ui

import actual.core.ui.PreviewActualScreen
import actual.core.ui.ActualScreenPreview
import actual.core.ui.LocalTheme
import actual.core.ui.Theme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun ContentLoading(
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Box(
    modifier = modifier,
    contentAlignment = Alignment.Center,
  ) {
    CircularProgressIndicator(
      modifier = Modifier.size(50.dp),
      color = theme.buttonPrimaryBackground,
    )
  }
}

@ActualScreenPreview
@Composable
private fun Loading() = PreviewActualScreen {
  ContentLoading()
}
