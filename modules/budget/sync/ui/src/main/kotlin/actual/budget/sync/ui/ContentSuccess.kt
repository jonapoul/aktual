package actual.budget.sync.ui

import actual.budget.sync.res.BudgetSyncStrings
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewScreen
import actual.core.ui.ScreenPreview
import actual.core.ui.Theme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun ContentSuccess(
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Box(
      modifier = Modifier.padding(CirclePadding),
      contentAlignment = Alignment.Center,
    ) {
      CircularProgressIndicator(
        modifier = Modifier.size(CircleSize),
        color = theme.successText,
        trackColor = theme.successText,
        strokeWidth = StrokeWidth,
        progress = { 1f },
      )

      Icon(
        modifier = Modifier.size(150.dp),
        imageVector = Icons.Filled.Check,
        contentDescription = BudgetSyncStrings.syncSuccess,
        tint = theme.successText,
      )
    }
  }
}

@ScreenPreview
@Composable
private fun Success() = PreviewScreen {
  ContentSuccess()
}
