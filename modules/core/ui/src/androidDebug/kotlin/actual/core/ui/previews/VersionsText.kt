package actual.core.ui.previews

import actual.core.model.ActualVersions
import actual.core.ui.PreviewThemedColumn
import actual.core.ui.VersionsText
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun PreviewServerNull() = PreviewThemedColumn {
  VersionsText(ActualVersions(app = "1.2.3", server = null))
}

@Preview
@Composable
private fun PreviewBothVersions() = PreviewThemedColumn {
  VersionsText(ActualVersions(app = "1.2.3", server = "2.3.4"))
}
