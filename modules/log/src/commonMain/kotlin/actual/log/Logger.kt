package actual.log

@Suppress("TooManyFunctions")
object Logger : Branch {
  override val explicitTag = ThreadLocal<String>()

  override fun v(message: String, vararg args: Any?) = branchArray.forEach { it.v(message, *args) }

  override fun v(t: Throwable?, message: String, vararg args: Any?) =
    branchArray.forEach { it.v(t, message, *args) }

  override fun v(t: Throwable?) = branchArray.forEach { it.v(t) }

  override fun d(message: String, vararg args: Any?) = branchArray.forEach { it.d(message, *args) }

  override fun d(t: Throwable?, message: String, vararg args: Any?) =
    branchArray.forEach { it.d(t, message, *args) }

  override fun d(t: Throwable?) = branchArray.forEach { it.d(t) }

  override fun i(message: String, vararg args: Any?) = branchArray.forEach { it.i(message, *args) }

  override fun i(t: Throwable?, message: String, vararg args: Any?) =
    branchArray.forEach { it.i(t, message, *args) }

  override fun i(t: Throwable?) = branchArray.forEach { it.i(t) }

  override fun w(message: String, vararg args: Any?) = branchArray.forEach { it.w(message, *args) }

  override fun w(t: Throwable?, message: String, vararg args: Any?) =
    branchArray.forEach { it.w(t, message, *args) }

  override fun w(t: Throwable?) = branchArray.forEach { it.w(t) }

  override fun e(message: String, vararg args: Any?) = branchArray.forEach { it.e(message, *args) }

  override fun e(t: Throwable?, message: String, vararg args: Any?) =
    branchArray.forEach { it.e(t, message, *args) }

  override fun e(t: Throwable?) = branchArray.forEach { it.e(t) }

  override fun wtf(message: String, vararg args: Any?) =
    branchArray.forEach { it.wtf(message, *args) }

  override fun wtf(t: Throwable?, message: String, vararg args: Any?) =
    branchArray.forEach { it.wtf(t, message, *args) }

  override fun wtf(t: Throwable?) = branchArray.forEach { it.wtf(t) }

  override fun log(priority: Int, t: Throwable?) = branchArray.forEach { it.log(priority, t) }

  override fun log(priority: Int, message: String, vararg args: Any?) =
    branchArray.forEach { it.log(priority, message, *args) }

  override fun log(priority: Int, t: Throwable?, message: String, vararg args: Any?) =
    branchArray.forEach { it.log(priority, t, message, *args) }

  fun tag(tag: String): Branch {
    for (branch in branchArray) branch.explicitTag.set(tag)
    return this
  }

  fun plant(branch: Branch) {
    require(branch !== this) { "Cannot plant Timber into itself." }
    synchronized(branchList) {
      branchList.add(branch)
      branchArray = branchList.toTypedArray()
    }
  }

  fun uproot(tree: Branch) {
    synchronized(branchList) {
      require(branchList.remove(tree)) { "Cannot uproot tree which is not planted: $tree" }
      branchArray = branchList.toTypedArray()
    }
  }

  fun uprootAll() {
    synchronized(branchList) {
      branchList.clear()
      branchArray = emptyArray()
    }
  }

  @Volatile
  private var branchArray = emptyArray<Branch>()
  private val branchList = ArrayList<Branch>()
}
