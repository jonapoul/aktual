package aktual.test

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.buildDatabase
import aktual.budget.model.BudgetId
import alakazam.test.TestCoroutineContexts
import alakazam.test.standardDispatcher
import androidx.room3.RoomDatabase
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

fun runDatabaseTest(
  provider: BudgetDatabase.BuilderProvider = inMemory(),
  action: suspend DatabaseTestScope.() -> Unit,
) = runTest {
  val contexts = TestCoroutineContexts(standardDispatcher)
  val databases = mutableListOf<BudgetDatabase>()
  val scope = DatabaseTestScopeImpl(provider, scope = this, contexts, databases)
  try {
    scope.action()
  } finally {
    databases.forEach { it.close() }
  }
}

fun inMemory(): BudgetDatabase.BuilderProvider = BudgetDatabase.BuilderProvider { _ ->
  inMemoryDatabaseBuilder()
}

internal expect fun inMemoryDatabaseBuilder(): RoomDatabase.Builder<BudgetDatabase>

@DslMarker annotation class DatabaseTestMarker

@DatabaseTestMarker
interface DatabaseTestScope {
  val provider: BudgetDatabase.BuilderProvider
  val scope: TestScope
  val contexts: TestCoroutineContexts

  fun buildDatabase(id: BudgetId): BudgetDatabase

  fun buildDatabase(): BudgetDatabase = buildDatabase(id = BudgetId("unused"))
}

private class DatabaseTestScopeImpl(
  override val provider: BudgetDatabase.BuilderProvider,
  override val scope: TestScope,
  override val contexts: TestCoroutineContexts,
  private val databases: MutableList<BudgetDatabase>,
) : DatabaseTestScope {
  override fun buildDatabase(id: BudgetId): BudgetDatabase {
    val db = provider(id).buildDatabase(contexts.io)
    databases += db
    return db
  }
}
