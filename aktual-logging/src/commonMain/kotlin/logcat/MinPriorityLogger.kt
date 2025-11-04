/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package logcat

interface MinPriorityLogger : LogcatLogger {
  val minPriority: LogPriority

  override fun isLoggable(priority: LogPriority, tag: String) = priority >= minPriority
}
