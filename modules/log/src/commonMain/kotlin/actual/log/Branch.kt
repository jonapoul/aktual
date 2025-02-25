package actual.log

@Suppress("TooManyFunctions")
interface Branch {
  val explicitTag: ThreadLocal<String>

  fun v(message: String, vararg args: Any?)
  fun v(t: Throwable?, message: String, vararg args: Any?)
  fun v(t: Throwable?)

  fun d(message: String, vararg args: Any?)
  fun d(t: Throwable?, message: String, vararg args: Any?)
  fun d(t: Throwable?)

  fun i(message: String, vararg args: Any?)
  fun i(t: Throwable?, message: String, vararg args: Any?)
  fun i(t: Throwable?)

  fun w(message: String, vararg args: Any?)
  fun w(t: Throwable?, message: String, vararg args: Any?)
  fun w(t: Throwable?)

  fun e(message: String, vararg args: Any?)
  fun e(t: Throwable?, message: String, vararg args: Any?)
  fun e(t: Throwable?)

  fun wtf(message: String, vararg args: Any?)
  fun wtf(t: Throwable?, message: String, vararg args: Any?)
  fun wtf(t: Throwable?)

  fun log(priority: Int, message: String, vararg args: Any?)
  fun log(priority: Int, t: Throwable?, message: String, vararg args: Any?)
  fun log(priority: Int, t: Throwable?)
}
