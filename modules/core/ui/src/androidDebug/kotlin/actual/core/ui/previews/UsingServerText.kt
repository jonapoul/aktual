package actual.core.ui.previews

import actual.core.model.ServerUrl
import actual.core.ui.PreviewThemedColumn
import actual.core.ui.UsingServerText
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun DemoServer() = PreviewThemedColumn {
  UsingServerText(
    url = ServerUrl.Demo,
    onClickChange = {},
  )
}

@Preview
@Composable
private fun NoServer() = PreviewThemedColumn {
  UsingServerText(
    url = null,
    onClickChange = {},
  )
}
