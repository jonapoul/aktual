/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
