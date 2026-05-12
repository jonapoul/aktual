package aktual.di

interface ScopeLifecycle : Initializable, Closeable

fun interface Initializable {
  fun initialize()
}

fun interface Closeable : AutoCloseable {
  override fun close()
}
