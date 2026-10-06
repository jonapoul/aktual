@file:Suppress("ClassName", "MatchingDeclarationName", "DEPRECATION")

package logcat

object logcat {
  context(subject: Any)
  inline fun v(tag: String? = null, t: Throwable? = null, message: () -> String) =
    combined1(VERBOSE, tag, t, message)

  context(subject: Any)
  inline fun d(tag: String? = null, t: Throwable? = null, message: () -> String) =
    combined1(DEBUG, tag, t, message)

  context(subject: Any)
  inline fun i(tag: String? = null, t: Throwable? = null, message: () -> String) =
    combined1(INFO, tag, t, message)

  context(subject: Any)
  inline fun w(tag: String? = null, t: Throwable? = null, message: () -> String) =
    combined1(WARN, tag, t, message)

  context(subject: Any)
  inline fun e(tag: String? = null, t: Throwable? = null, message: () -> String) =
    combined1(ERROR, tag, t, message)

  context(subject: Any)
  inline fun wtf(tag: String? = null, t: Throwable? = null, message: () -> String) =
    combined1(ASSERT, tag, t, message)

  context(subject: Any)
  inline fun v(t: Throwable? = null, message: () -> String) =
    combined1(VERBOSE, tag = null, t, message)

  context(subject: Any)
  inline fun d(t: Throwable? = null, message: () -> String) =
    combined1(DEBUG, tag = null, t, message)

  context(subject: Any)
  inline fun i(t: Throwable? = null, message: () -> String) =
    combined1(INFO, tag = null, t, message)

  context(subject: Any)
  inline fun w(t: Throwable? = null, message: () -> String) =
    combined1(WARN, tag = null, t, message)

  context(subject: Any)
  inline fun e(t: Throwable? = null, message: () -> String) =
    combined1(ERROR, tag = null, t, message)

  context(subject: Any)
  inline fun wtf(t: Throwable? = null, message: () -> String) =
    combined1(ASSERT, tag = null, t, message)

  inline fun v(tag: String, t: Throwable? = null, message: () -> String) =
    combined2(VERBOSE, tag, t, message)

  inline fun d(tag: String, t: Throwable? = null, message: () -> String) =
    combined2(DEBUG, tag, t, message)

  inline fun i(tag: String, t: Throwable? = null, message: () -> String) =
    combined2(INFO, tag, t, message)

  inline fun w(tag: String, t: Throwable? = null, message: () -> String) =
    combined2(WARN, tag, t, message)

  inline fun e(tag: String, t: Throwable? = null, message: () -> String) =
    combined2(ERROR, tag, t, message)

  inline fun wtf(tag: String, t: Throwable? = null, message: () -> String) =
    combined2(ASSERT, tag, t, message)

  @Deprecated("Not intended to be used directly")
  context(subject: Any)
  inline fun combined1(priority: LogPriority, tag: String?, t: Throwable?, message: () -> String) {
    subject.logcat(priority, tag, message)
    if (t != null) {
      subject.logcat(priority, tag) { t.asLog() }
    }
  }

  @Deprecated("Not intended to be used directly")
  inline fun combined2(priority: LogPriority, tag: String?, t: Throwable?, message: () -> String) {
    logcat(priority, tag, message)
    if (t != null) {
      logcat(priority, tag) { t.asLog() }
    }
  }
}
