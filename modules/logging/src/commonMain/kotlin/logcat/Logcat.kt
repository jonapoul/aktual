/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
@file:Suppress("ClassName", "MatchingDeclarationName", "DEPRECATION")

package logcat

object logcat {
  context(subject: Any)
  inline fun v(tag: String? = null, t: Throwable? = null, message: () -> String) =
    combined(LogPriority.VERBOSE, tag, t, message)

  context(subject: Any)
  inline fun d(tag: String? = null, t: Throwable? = null, message: () -> String) =
    combined(LogPriority.DEBUG, tag, t, message)

  context(subject: Any)
  inline fun i(tag: String? = null, t: Throwable? = null, message: () -> String) =
    combined(LogPriority.INFO, tag, t, message)

  context(subject: Any)
  inline fun w(tag: String? = null, t: Throwable? = null, message: () -> String) =
    combined(LogPriority.WARN, tag, t, message)

  context(subject: Any)
  inline fun e(tag: String? = null, t: Throwable? = null, message: () -> String) =
    combined(LogPriority.ERROR, tag, t, message)

  context(subject: Any)
  inline fun wtf(tag: String? = null, t: Throwable? = null, message: () -> String) =
    combined(LogPriority.ASSERT, tag, t, message)

  inline fun v(t: Throwable? = null, message: () -> String) = v(null, t, message)
  inline fun d(t: Throwable? = null, message: () -> String) = d(null, t, message)
  inline fun i(t: Throwable? = null, message: () -> String) = i(null, t, message)
  inline fun w(t: Throwable? = null, message: () -> String) = w(null, t, message)
  inline fun e(t: Throwable? = null, message: () -> String) = e(null, t, message)
  inline fun wtf(t: Throwable? = null, message: () -> String) = wtf(null, t, message)

  @Deprecated("Not intended to be used directly")
  context(subject: Any)
  inline fun combined(priority: LogPriority, tag: String?, t: Throwable?, message: () -> String) {
    subject.logcat(priority, tag, message)
    if (t != null) {
      subject.logcat(priority, tag) { t.asLog() }
    }
  }
}
