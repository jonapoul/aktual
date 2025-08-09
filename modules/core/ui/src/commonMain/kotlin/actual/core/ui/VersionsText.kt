package actual.core.ui

import actual.core.model.ActualVersions
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun VersionsText(
  versions: ActualVersions,
  modifier: Modifier = Modifier,
  padding: Dp = 15.dp,
) = Text(
  modifier = modifier.padding(padding),
  text = versions.toString(),
  style = ActualTypography.labelMedium,
)
