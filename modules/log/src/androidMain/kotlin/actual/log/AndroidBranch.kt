package actual.log

import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter

@Suppress("TooManyFunctions")
open class AndroidBranch : Branch {
  override val explicitTag = ThreadLocal<String>()

  open val tag: String?
    get() {
      val tag = explicitTag.get()
      if (tag != null) {
        explicitTag.remove()
      }
      return tag
    }

  override fun v(message: String, vararg args: Any?) = prepareLog(Log.VERBOSE, null, message, *args)
  override fun v(t: Throwable?, message: String, vararg args: Any?) = prepareLog(Log.VERBOSE, t, message, *args)
  override fun v(t: Throwable?) = prepareLog(Log.VERBOSE, t, null)

  override fun d(message: String, vararg args: Any?) = prepareLog(Log.DEBUG, null, message, *args)
  override fun d(t: Throwable?, message: String, vararg args: Any?) = prepareLog(Log.DEBUG, t, message, *args)
  override fun d(t: Throwable?) = prepareLog(Log.DEBUG, t, null)

  override fun i(message: String, vararg args: Any?) = prepareLog(Log.INFO, null, message, *args)
  override fun i(t: Throwable?, message: String, vararg args: Any?) = prepareLog(Log.INFO, t, message, *args)
  override fun i(t: Throwable?) = prepareLog(Log.INFO, t, null)

  override fun w(message: String, vararg args: Any?) = prepareLog(Log.WARN, null, message, *args)
  override fun w(t: Throwable?, message: String, vararg args: Any?) = prepareLog(Log.WARN, t, message, *args)
  override fun w(t: Throwable?) = prepareLog(Log.WARN, t, null)

  override fun e(message: String, vararg args: Any?) = prepareLog(Log.ERROR, null, message, *args)
  override fun e(t: Throwable?, message: String, vararg args: Any?) = prepareLog(Log.ERROR, t, message, *args)
  override fun e(t: Throwable?) = prepareLog(Log.ERROR, t, null)

  override fun wtf(message: String, vararg args: Any?) = prepareLog(Log.ASSERT, null, message, *args)
  override fun wtf(t: Throwable?) = prepareLog(Log.ASSERT, t, null)
  override fun wtf(t: Throwable?, message: String, vararg args: Any?) =
    prepareLog(Log.ASSERT, t, message, *args)

  override fun log(priority: Int, message: String, vararg args: Any?) = prepareLog(priority, null, message, *args)
  override fun log(priority: Int, t: Throwable?) = prepareLog(priority, t, null)
  override fun log(priority: Int, t: Throwable?, message: String, vararg args: Any?) =
    prepareLog(priority, t, message, *args)

  private fun prepareLog(priority: Int, t: Throwable?, message: String?, vararg args: Any?) {
    // Consume tag even when message is not loggable so that next message is correctly tagged.
    val tag = tag

    var msg = message
    if (msg.isNullOrEmpty()) {
      if (t == null) {
        return // Swallow message if it's null and there's no throwable.
      }
      msg = getStackTraceString(t)
    } else {
      if (args.isNotEmpty()) {
        msg = formatMessage(msg, args)
      }
      if (t != null) {
        msg += "\n" + getStackTraceString(t)
      }
    }

    log(priority, tag, msg, t)
  }

  @Suppress("SpreadOperator")
  protected open fun formatMessage(message: String, args: Array<out Any?>) = message.format(*args)

  private fun getStackTraceString(t: Throwable): String {
    // Don't replace this with Log.getStackTraceString() - it hides
    // UnknownHostException, which is not what we want.
    val sw = StringWriter(TAG_SIZE)
    val pw = PrintWriter(sw, false)
    t.printStackTrace(pw)
    pw.flush()
    return sw.toString()
  }

  protected open fun log(priority: Int, tag: String?, message: String, t: Throwable?) = Unit

  private companion object {
    const val TAG_SIZE = 256
  }
}
