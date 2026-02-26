@file:Suppress("ClassName", "MatchingDeclarationName", "DEPRECATION", "FunctionNameMinLength")

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

  context(subject: Any)
  inline fun v(t: Throwable? = null, message: () -> String) =
    combined(LogPriority.VERBOSE, tag = null, t, message)

  context(subject: Any)
  inline fun d(t: Throwable? = null, message: () -> String) =
    combined(LogPriority.DEBUG, tag = null, t, message)

  context(subject: Any)
  inline fun i(t: Throwable? = null, message: () -> String) =
    combined(LogPriority.INFO, tag = null, t, message)

  context(subject: Any)
  inline fun w(t: Throwable? = null, message: () -> String) =
    combined(LogPriority.WARN, tag = null, t, message)

  context(subject: Any)
  inline fun e(t: Throwable? = null, message: () -> String) =
    combined(LogPriority.ERROR, tag = null, t, message)

  context(subject: Any)
  inline fun wtf(t: Throwable? = null, message: () -> String) =
    combined(LogPriority.ASSERT, tag = null, t, message)

  inline fun v(tag: String, t: Throwable? = null, message: () -> String) =
    combined(LogPriority.VERBOSE, tag, t, message)

  inline fun d(tag: String, t: Throwable? = null, message: () -> String) =
    combined(LogPriority.DEBUG, tag, t, message)

  inline fun i(tag: String, t: Throwable? = null, message: () -> String) =
    combined(LogPriority.INFO, tag, t, message)

  inline fun w(tag: String, t: Throwable? = null, message: () -> String) =
    combined(LogPriority.WARN, tag, t, message)

  inline fun e(tag: String, t: Throwable? = null, message: () -> String) =
    combined(LogPriority.ERROR, tag, t, message)

  inline fun wtf(tag: String, t: Throwable? = null, message: () -> String) =
    combined(LogPriority.ASSERT, tag, t, message)

  @Deprecated("Not intended to be used directly")
  context(subject: Any)
  inline fun combined(priority: LogPriority, tag: String?, t: Throwable?, message: () -> String) {
    subject.logcat(priority, tag, message)
    if (t != null) {
      subject.logcat(priority, tag) { t.asLog() }
    }
  }

  @Deprecated("Not intended to be used directly")
  inline fun combined(priority: LogPriority, tag: String?, t: Throwable?, message: () -> String) {
    logcat(priority, tag, message)
    if (t != null) {
      logcat(priority, tag) { t.asLog() }
    }
  }
}
