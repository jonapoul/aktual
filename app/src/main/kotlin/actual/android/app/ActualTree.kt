package actual.android.app

import timber.log.Timber

internal class ActualTree : Timber.DebugTree() {
  override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
    super.log(priority, tag, "ACTUAL: $message", t)
  }

  override fun createStackElementTag(element: StackTraceElement) = "${element.fileName}:${element.lineNumber}"
}
