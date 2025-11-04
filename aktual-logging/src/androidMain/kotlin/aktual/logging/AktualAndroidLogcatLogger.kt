/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.logging

import logcat.AndroidLogcatLogger
import logcat.LogPriority
import logcat.MinPriorityLogger

class AktualAndroidLogcatLogger(override val minPriority: LogPriority) : MinPriorityLogger {
  private val delegate = AndroidLogcatLogger(minPriority)

  override fun log(priority: LogPriority, tag: String, message: String) {
    delegate.log(priority, tag, message = "ACTUAL: $message")
  }
}
