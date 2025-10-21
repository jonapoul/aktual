/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.core.model

fun interface UrlOpener {
  operator fun invoke(url: String)
}
