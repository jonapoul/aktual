/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.core.model

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.awt.Desktop
import java.net.URI

@Inject
@ContributesBinding(AppScope::class)
class JvmUrlOpener : UrlOpener {
  override fun invoke(url: String) {
    if (Desktop.isDesktopSupported()) {
      Desktop.getDesktop().browse(URI(url))
    } else {
      error("Opening URLs not supported on this device - tried to open $url")
    }
  }
}
