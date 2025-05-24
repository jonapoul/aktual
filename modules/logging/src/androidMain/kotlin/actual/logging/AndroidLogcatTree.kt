package actual.logging

import alakazam.kotlin.logging.DebugAndroidTree

internal class AndroidLogcatTree : DebugAndroidTree() {
  override fun createStackElementTag(element: StackTraceElement) = "${element.fileName}:${element.lineNumber}"

  override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
    super.log(priority, tag, "ACTUAL: $message", t)
  }
}
