package aktual.budget.db

import aktual.budget.db.test.insertRule
import aktual.budget.model.BudgetId
import aktual.budget.model.RuleId
import aktual.test.inMemoryDriverFactory
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

internal class RulesTest {
  @Test
  fun `Enum columns are stored as their serial names`() = runRawDatabaseTest { driver, db ->
    db.insertRule(id = ID, stage = Pre, conditionsOp = Or)

    assertThat(driver.rawEnumColumns()).isEqualTo("pre" to "or")
  }

  @Test
  fun `Unrecognised enum column values decode as Unknown`() = runRawDatabaseTest { driver, db ->
    // as if written by a newer client
    driver
      .execute(
        identifier = null,
        sql =
          "INSERT INTO rules(id, stage, conditions, actions, conditions_op) " +
            "VALUES ('$ID', 'something-new', '[]', '[]', 'xor')",
        parameters = 0,
      )
      .await()

    val rule = db.rulesQueries.get(RuleId(ID)).awaitAsOne()
    assertThat(rule.stage).isEqualTo(Unknown)
    assertThat(rule.conditions_op).isEqualTo(Unknown)
  }

  private suspend fun SqlDriver.rawEnumColumns(): Pair<String?, String?> =
    executeQuery(
        identifier = null,
        sql = "SELECT stage, conditions_op FROM rules WHERE id = '$ID'",
        parameters = 0,
        // read eagerly, the statement is closed once the mapper returns
        mapper = { cursor ->
          cursor.next()
          QueryResult.Value(cursor.getString(0) to cursor.getString(1))
        },
      )
      .await()

  private fun runRawDatabaseTest(action: suspend (SqlDriver, BudgetDatabase) -> Unit) = runTest {
    val driver = inMemoryDriverFactory().create(BudgetId("abc-123"))
    val db = buildDatabase(driver)
    driver.use { action(driver, db) }
  }

  private companion object {
    const val ID = "rule-1"
  }
}
