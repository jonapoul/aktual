package actual.db

import actual.test.DatabaseRule
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule

internal abstract class DatabaseTest {
  @get:Rule
  val databaseRule = DatabaseRule.inMemory()

  protected fun runDatabaseTest(
    block: suspend BudgetDatabase.(TestScope) -> Unit,
  ) = runTest { block(databaseRule.database, this) }
}
