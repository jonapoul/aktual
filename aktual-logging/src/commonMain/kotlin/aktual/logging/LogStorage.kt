/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.logging

import okio.Path

fun interface LogStorage {
  fun directory(): Path
}
