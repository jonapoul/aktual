/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.core.model

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.io.InputStream

@Inject
@ContributesBinding(AppScope::class)
class JvmAssets : Assets {
  // Without the /, it looks relative to the class's package. With /, it looks at the classpath root
  override fun getStream(name: String): InputStream =
    javaClass.getResourceAsStream("/$name") ?: error("Failed to load '$name' from resources")
}
