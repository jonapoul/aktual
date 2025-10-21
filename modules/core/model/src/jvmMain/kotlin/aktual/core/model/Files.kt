/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.core.model

import okio.Path
import okio.Path.Companion.toOkioPath
import java.io.File

interface Files {
  fun userHome(): Path
}

object SystemFiles : Files {
  override fun userHome(): Path = System
    .getProperty("user.home")
    .let(::File)
    .toOkioPath()
    .resolve(".aktual")
}
