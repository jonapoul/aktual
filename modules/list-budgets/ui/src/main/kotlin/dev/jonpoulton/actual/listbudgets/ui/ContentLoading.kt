package dev.jonpoulton.actual.listbudgets.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.jonpoulton.actual.core.ui.ActualColorScheme
import dev.jonpoulton.actual.core.ui.ActualScreenPreview
import dev.jonpoulton.actual.core.ui.LocalActualColorScheme
import dev.jonpoulton.actual.core.ui.PreviewActualScreen

@Composable
internal fun ContentLoading(
  modifier: Modifier = Modifier,
  colors: ActualColorScheme = LocalActualColorScheme.current,
) {
  Box(
    modifier = modifier,
    contentAlignment = Alignment.Center,
  ) {
    CircularProgressIndicator(
      modifier = Modifier.size(100.dp),
      color = colors.buttonPrimaryBackground,
    )
  }
}

@ActualScreenPreview
@Composable
private fun Loading() = PreviewActualScreen {
  ContentLoading()
}
