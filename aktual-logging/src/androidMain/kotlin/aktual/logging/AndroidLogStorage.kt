/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.logging

import android.content.Context
import okio.Path
import okio.Path.Companion.toOkioPath

class AndroidLogStorage(private val context: Context) : LogStorage {
  override fun directory(): Path = context.filesDir.resolve("../logs").toOkioPath()
}
