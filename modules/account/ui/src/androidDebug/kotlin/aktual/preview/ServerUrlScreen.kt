/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.preview

import aktual.account.ui.url.ServerUrlScaffold
import aktual.core.model.AktualVersions
import aktual.core.model.Protocol
import aktual.core.ui.PreviewThemedScreen
import aktual.core.ui.TripleScreenPreview
import androidx.compose.runtime.Composable

@TripleScreenPreview
@Composable
private fun Regular() = PreviewThemedScreen {
  ServerUrlScaffold(
    url = "",
    protocol = Protocol.Https,
    versions = AktualVersions.Dummy,
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
    versions = AktualVersions.Dummy,
    isEnabled = true,
    isLoading = true,
    onAction = {},
    errorMessage = "Hello this is an error message, split over multiple lines so you can see how it behaves",
  )
}
