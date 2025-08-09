package actual.preview

import actual.account.ui.url.InputFields
import actual.core.model.Protocol
import actual.core.ui.PreviewThemedColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun Empty() = PreviewThemedColumn {
  InputFields(
    url = "",
    protocol = Protocol.Http,
    onAction = {},
  )
}

@Preview
@Composable
private fun Filled() = PreviewThemedColumn {
  InputFields(
    url = "my.server.com:1234/path",
    protocol = Protocol.Https,
    onAction = {},
  )
}
