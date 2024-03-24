package dev.jonpoulton.actual.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.jonpoulton.actual.core.model.ActualVersions

@Composable
fun VersionsText(
  versions: ActualVersions,
  modifier: Modifier = Modifier,
  padding: Dp = 15.dp,
  fontSize: TextUnit = 13.sp,
) {
  val colorScheme = LocalActualColorScheme.current
  Text(
    modifier = modifier.padding(padding),
    text = versions.toString(),
    fontSize = fontSize,
    color = colorScheme.pageTextSubdued,
  )
}

@Preview
@Composable
private fun PreviewServerNull() = PreviewActualColumn {
  VersionsText(ActualVersions(app = "1.2.3", server = null))
}

@Preview
@Composable
private fun PreviewBothVersions() = PreviewActualColumn {
  VersionsText(ActualVersions(app = "1.2.3", server = "2.3.4"))
}
