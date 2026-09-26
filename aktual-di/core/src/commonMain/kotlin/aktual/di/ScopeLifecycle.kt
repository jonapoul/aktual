package aktual.di

// Contribute into both sets with the same scope qualifier, see Initializable and Closeable
interface ScopeLifecycle : Initializable, Closeable

// Run when the owning graph is created. Contribute with the graph's scope as a qualifier, e.g.
// binding<@ForScope(BudgetScope::class) Initializable>(), otherwise child graphs re-run it
fun interface Initializable {
  fun initialize()
}

// Run when the owning graph is torn down. Contribute with the graph's scope as a qualifier, e.g.
// binding<@ForScope(BudgetScope::class) Closeable>(), otherwise closing a child graph closes it too
fun interface Closeable : AutoCloseable {
  override fun close()
}
