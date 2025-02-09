package actual.core.ui

import actual.core.versions.ActualVersions
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun VersionsText(
  versions: ActualVersions,
  modifier: Modifier = Modifier,
  padding: Dp = 15.dp,
) {
  Text(
    modifier = modifier.padding(padding),
    text = versions.toString(),
    style = MaterialTheme.typography.labelMedium,
  )
}

@Preview
@Composable
private fun PreviewServerNull() = PreviewColumn {
  VersionsText(ActualVersions(app = "1.2.3", server = null))
}

@Preview
@Composable
private fun PreviewBothVersions() = PreviewColumn {
  VersionsText(ActualVersions(app = "1.2.3", server = "2.3.4"))
}
