package actual.android.app

import alakazam.kotlin.logging.DebugAndroidTree

internal class ActualTree : DebugAndroidTree() {
  override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
    super.log(priority, tag, "ACTUAL: $message", t)
  }

  override fun createStackElementTag(element: StackTraceElement) = "${element.fileName}:${element.lineNumber}"
}
