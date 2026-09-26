package aktual.budget.db.dao

import aktual.budget.model.WidgetId
import aktual.test.runDatabaseTest
import alakazam.test.TestCoroutineContexts
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

internal class DashboardDaoTest {
  @Test
  fun `Renaming keeps values we don't recognise`() = runDaoTest {
    val id = WidgetId("abc-123")
    insert(
      id,
      type = NetWorth,
      x = 0,
      y = 0,
      meta = json("""{"name":"old","mode":"new-mode","extra":1}"""),
    )

    rename(id, "new")

    val meta = observeAll().first().single().meta
    assertThat(meta).isEqualTo(json("""{"name":"new","mode":"new-mode","extra":1}"""))
  }

  private fun json(string: String): JsonObject = Json.decodeFromString(string)

  private fun runDaoTest(action: suspend DashboardDao.(TestScope) -> Unit) =
    runDatabaseTest { scope ->
      val dao =
        DashboardDao(this, TestCoroutineContexts(StandardTestDispatcher(scope.testScheduler)))
      action(dao, scope)
    }
}
