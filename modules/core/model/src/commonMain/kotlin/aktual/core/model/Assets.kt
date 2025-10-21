/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.core.model

import java.io.InputStream

fun interface Assets {
  fun getStream(name: String): InputStream
}
