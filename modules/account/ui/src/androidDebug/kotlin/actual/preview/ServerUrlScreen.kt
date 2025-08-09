package actual.preview

import actual.account.ui.url.ServerUrlScaffold
import actual.core.model.ActualVersions
import actual.core.model.Protocol
import actual.core.ui.PreviewThemedScreen
import actual.core.ui.TripleScreenPreview
import androidx.compose.runtime.Composable

@TripleScreenPreview
@Composable
private fun Regular() = PreviewThemedScreen {
  ServerUrlScaffold(
    url = "",
    protocol = Protocol.Https,
    versions = ActualVersions.Dummy,
    isEnabled = true,
    isLoading = false,
    onAction = {},
    errorMessage = null,
  )
}

@TripleScreenPreview
@Composable
private fun WithErrorMessage() = PreviewThemedScreen {
  ServerUrlScaffold(
    url = "my.server.com:1234/path",
    protocol = Protocol.Http,
    versions = ActualVersions.Dummy,
    isEnabled = true,
    isLoading = true,
    onAction = {},
    errorMessage = "Hello this is an error message, split over multiple lines so you can see how it behaves",
  )
}
