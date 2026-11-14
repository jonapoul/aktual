package aktual.budget.db

import aktual.budget.db.test.insertRule
import aktual.budget.model.BudgetId
import aktual.budget.model.Condition
import aktual.budget.model.Field
import aktual.budget.model.RuleAction
import aktual.budget.model.RuleId
import aktual.test.inMemoryDriverFactory
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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

  @Test
  fun `Fields stored with upstream's internal names decode as the public ones`() =
    runRawDatabaseTest { driver, db ->
      val conditions =
        """[{"field":"description","op":"is","value":"p"},""" +
          """{"field":"acct","op":"is","value":"a"},""" +
          """{"field":"imported_description","op":"contains","value":"x"}]"""
      val actions = """[{"field":"description","op":"set","value":"p","type":"id"}]"""
      driver
        .execute(
          identifier = null,
          sql =
            "INSERT INTO rules(id, conditions, actions) VALUES ('$ID', '$conditions', '$actions')",
          parameters = 0,
        )
        .await()

      val rule = db.rulesQueries.get(RuleId(ID)).awaitAsOne()
      assertThat(rule.conditions.orEmpty().map { it.field })
        .containsExactly(Field.Payee, Field.Account, Field.ImportedPayee)
      assertThat(rule.actions.orEmpty().map { it.field }).containsExactly(Field.Payee)
    }

  @Test
  fun `Fields are stored with upstream's internal names`() = runRawDatabaseTest { driver, db ->
    db.insertRule(
      id = ID,
      conditions =
        listOf(
          Condition(field = Payee, operator = Is, value = JsonPrimitive("p")),
          Condition(field = Account, operator = Is, value = JsonPrimitive("a")),
          Condition(field = ImportedPayee, operator = Contains, value = JsonPrimitive("x")),
          Condition(field = Category, operator = Is, value = JsonPrimitive("c")),
        ),
      actions =
        listOf(RuleAction(value = JsonPrimitive("p"), op = RuleAction.Op.Set, field = Payee)),
    )

    val (conditions, actions) = driver.rawJsonColumns()
    assertThat(conditions.fields())
      .containsExactly("description", "acct", "imported_description", "category")
    assertThat(actions.fields()).containsExactly("description")
  }

  private fun String.fields(): List<String> =
    Json.parseToJsonElement(this).jsonArray.map { it.jsonObject["field"]!!.jsonPrimitive.content }

  private suspend fun SqlDriver.rawJsonColumns(): Pair<String, String> =
    executeQuery(
        identifier = null,
        sql = "SELECT conditions, actions FROM rules WHERE id = '$ID'",
        parameters = 0,
        mapper = { cursor ->
          cursor.next()
          QueryResult.Value(cursor.getString(0).orEmpty() to cursor.getString(1).orEmpty())
        },
      )
      .await()

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
