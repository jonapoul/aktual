package dev.jonpoulton.actual.core.ui

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable

@Composable
fun PreviewActual(
  content: @Composable () -> Unit,
) {
  ActualTheme {
    Surface {
      content()
    }
  }
}
